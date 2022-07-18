import commander from 'commander';
import path from 'path';
import fs from 'fs';
import chalk from 'chalk';

import Debug from 'debug';
import fse from 'fs-extra';
import tar from 'tar';
import {Blob, FormData} from 'formdata-node';
import {fileFromPath} from 'formdata-node/file-from-path';

import {
    addPagesProject,
    addProjectsPageBundleRelations,
    checkProjectInfoByName,
    checkUserPagesDeployInfo,
    collectInfo,
    deployBundle,
    formatSize,
    loadConfig,
    remoteCheck
} from './utils.mjs';
import inquirer from "inquirer";

const debug = Debug('osrc:deploy');

commander
    .option('-d, --dirname <dirname>', 'dist directory')
    .action(async function (cmd) {
        debug('deploy.action', cmd);
        const {scope, accessToken, remote} = loadConfig();
        if (!accessToken || !remote || !scope || !scope.id || !scope.type) {
            console.log(
                chalk.red('config data error!please login again!'),
            );
            process.exit(-1);
        }


        const projectRoot = process.cwd();
        debug('projectRoot', projectRoot);

        const packageContent = fse.readFileSync(
            path.join(projectRoot, 'package.json'),
            'utf-8',
        );
        const packageJson = JSON.parse(packageContent);
        debug('packageJson', packageJson);

        const appInfo = collectInfo(packageJson);
        console.log(chalk.green('collect project info...'));

        if (!appInfo.name) {
            console.log(
                chalk.red('name is required, please fill it in package.json'),
            );
            process.exit(-1);
        }
        console.log(chalk.blue('name:' + appInfo.name));
        if (!appInfo.version) {
            console.log(
                chalk.red('version is required, please fill it in package.json'),
            );
            process.exit(-1);
        }
        console.log(chalk.blue('version:' + appInfo.version));

        const projectsBundleInfo = await checkUserPagesDeployInfo(appInfo.name);

        if (projectsBundleInfo.exist) {
            console.log(chalk.green("Found project “" + projectsBundleInfo.ownerName + "/" + projectsBundleInfo.name + "”. Link to "
                + "it?[Y/n/s(skip)]"));
            const {choose} = await inquirer.prompt([
                {
                    type: 'input',
                    name: 'choose',
                    validate(name) {
                        return String(name).trim().toLowerCase() === 'y' || String(name).trim().toLowerCase() === 'n' || String(name).trim().toLowerCase() === 's'
                    },
                }
            ]);

            switch (choose) {
                case 'y':
                    break;
                case 'n':
                    await bundleProject(appInfo, projectsBundleInfo);
                    break;
                case 's':
                default:
                    console.log('deploy canceled!')
                    process.exit(-1)
                    break;
            }
        } else {
            await bundleProject(appInfo, projectsBundleInfo);
        }

        const readmeFiles = fse
            .readdirSync(projectRoot)
            .filter((file) => file.toLocaleLowerCase() === 'readme.md');

        let readmeContent = '';
        if (readmeFiles.length) {
            readmeContent = fse.readFileSync(readmeFiles[0], 'utf-8');
        }

        debug('appInfo', appInfo);
        if (!cmd.dirname) {
            console.log(
                chalk.red('no specific build target, use default', chalk.green('dist'), 'dir'),
            );
        }

        const r = await remoteCheck(appInfo);
        console.log('remoteCheck', r);

        const _dirname = cmd.dirname || 'dist';
        console.log(chalk.green(_dirname) + 'will be deploy to osrc site');

        const dirname = _dirname;
        debug('dirname', dirname);
        process.chdir(dirname);
        console.log(chalk.green(`go to ${dirname} dir`));

        const tempFilepath = `${appInfo.name}_${
            appInfo.version
        }_${Date.now()}.tar.gz`;
        debug('bundleName', tempFilepath);

        const fullFilePath = path.join(projectRoot, tempFilepath);
        const files = fse.readdirSync('./');
        debug('files:', files);
        console.log(chalk.green(`package dir `));
        files.forEach((file) => {
            console.log('--', chalk.green(`${file}`));
        });

        await tar.create(
            {
                gzip: true,
                file: fullFilePath,
                // prefix:'dist/'
            },
            files,
        );

        const buffer = fs.readFileSync(fullFilePath);
        console.log(chalk.green('package size ' + formatSize(buffer.byteLength)));

        const formData = new FormData();

        formData.set('readme', readmeContent);
        const blob = new Blob([JSON.stringify(appInfo)], {
            type: 'application/json',
        });

        formData.set('pageInfo', blob);
        const file = await fileFromPath(fullFilePath);
        formData.set('files', file);
        debug('formData', [...formData.keys()]);

        console.log(chalk.green(`start deploy...`));
        await deployBundle(formData, projectsBundleInfo.projectId);
        console.log(chalk.green(`deploy sucess!`));
        fse.unlinkSync(fullFilePath);
    })
    .parse(process.argv);

async function bundleProject(appInfo, projectsBundleInfo) {
    console.log("Link to existing project? [Y/n]")
    const {choose} = await inquirer.prompt([
        {
            type: 'input',
            name: 'choose',
            validate(name) {
                return String(name).trim().toLowerCase() === 'y' || String(name).trim().toLowerCase() === 'n'
            },
        }
    ]);

    if (choose === 'y') {
        console.log("What’s the name of your existing project?")
        const {name} = await inquirer.prompt([
            {
                type: 'input',
                name: 'name',
                validate(name) {
                    return String(name).trim().length > 0
                },
            }
        ]);
        const projects = await checkProjectInfoByName(name);
        console.log('check projects info: ' + projects.id)
        projectsBundleInfo.projectId = projects.id
        await addProjectsPageBundleRelations(projects.id, appInfo.bundleStr)
    } else if (choose === 'n') {
        console.log("What’s your project’s name?")
        console.log("(project name can only consist of up to 100 "
            + "aiphanumeric lowercase characters.Hyphens can be used in "
            + "between the name,but never at the start or end.)")
        const {name} = await inquirer.prompt([
            {
                type: 'input',
                name: 'name',
                validate(name) {
                    return (/^(?!-)(?!.*?-$)[a-zA-Z0-9-]{1,100}$/i.test(name))
                },
            }
        ]);
        const projects = await addPagesProject(name, appInfo.bundleStr)
        console.log('add projects info: ' + projects.id)
        projectsBundleInfo.projectId = projects.id
    } else {
        console.log('deploy canceled!')
        process.exit(-1)
    }
}
