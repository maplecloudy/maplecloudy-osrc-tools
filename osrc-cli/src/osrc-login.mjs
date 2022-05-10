import commander from 'commander';
import inquirer from 'inquirer';
import {
    getUserInfo,
    login,
    loadConfig,
    getUserOrganizationAccessRole, getUserOrganizationsCount
} from './utils.mjs';
import Debug from 'debug';

const debug = Debug('osrc:login');

commander
    .action(async function () {
        const config = loadConfig();
        debug('login.action');
        const answers = await inquirer.prompt([
            {
                type: 'input',
                name: 'name',
                message: 'login name',
                validate(name) {
                    return String(name).trim().length >= 3;
                },
            },
            {
                name: 'password',
                type: 'password',
                validate(password) {
                    return String(password).trim().length >= 4;
                },
            },
        ]);

        debug('answers', answers);
        try {
            await login(answers.name, answers.password);
        } catch (e) {
            console.error(e);
        }

        console.log("Which scope do you want to deploy to? \n 1 "
            + "personal account \n 2 organization" + " ")
        const {scope} = await inquirer.prompt([
            {
                type: 'input',
                name: 'scope',
                validate(name) {
                    return Number(name) == 1 || Number(name) == 2;
                },
            }
        ]);
        if (scope == 1) {
            const user = getUserInfo();
        } else {
            await getUserOrganizationsCount()
            console.log("please input the organization name:")
            const {name} = await inquirer.prompt([
                {
                    type: 'input',
                    name: 'name',
                    validate(name) {
                        return String(name).trim().length >= 3
                    },
                }
            ]);
            await getUserOrganizationAccessRole(name)
        }
        console.log("Init osrc config successfully!")


    })
    .parse(process.argv);
