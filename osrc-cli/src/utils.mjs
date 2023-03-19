import path from 'path';
import fse from 'fs-extra';
import fetch from 'node-fetch';
import chalk from 'chalk';
import Debug from 'debug';

const debug = Debug('osrc:utils');

const home = process.env.HOME || process.env.USERPROFILE;
const filePath = path.join(home, '.osrc');

export function saveConfig(config) {
    fse.writeFileSync(filePath, JSON.stringify(config, null, 2), 'utf-8');
}

export function loadConfig() {
    fse.ensureFileSync(filePath);
    const content = fse.readFileSync(filePath, 'utf-8') || '{}';
    let config = {};
    try {
        config = JSON.parse(content);
    } catch (e) {
    }
    if (!config.remote) {
        config.remote = 'https://www.osrc.com';
    }
    if (!config.accessToken) {
        config.accessToken = process.env.OSRC_APP_TOKEN || undefined;
    }
    if (!config.scope){
        config.scope = {}
    }
    return config;
}

export function collectInfo(packageJson) {
    const {
        name,
        version,
        author,
        contributors,
        homepage,
        license,
        keywords,
        repository,
        description,
        main,
    } = packageJson;
    const infos = {
        bundle: name.split('/'),
        bundleStr: name,
        name,
        version,
        // author,
        // contributors,
        homepage,
        license,
        topics: keywords,
        // repository,
        description,
        // main,
    };
    const entries = Object.entries(infos).filter(([n, v]) => !!v);
    return Object.fromEntries(entries);
}

export async function login(username, password) {
    const config = loadConfig();
    debug('config', config);
    const response = await fetch(`${config.remote}/api/users/signin`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            username,
            password,
        }),
    });
    if (response.status === 200) {
        const data = await response.json();
        debug('login', data);
        config.username = username;
        config.accessToken = data.accessToken;
        config.tokenType = data.tokenType;
        saveConfig(config);
        console.log(chalk.green('login success'));
    } else {
        console.log(chalk.red('login failed!'));
        process.exit(-1);
    }
}

export async function deployBundle(formData, projectId,appInfo) {
    const {scope,accessToken, remote,username} = loadConfig();
    const url = `${remote}/api/pages/upload?projectId=` + projectId+"&type=" + scope.type + "&scopeId=" + scope.id;
    const resp = await fetch(url, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
        body: formData,
    });
    if (resp.status === 200) {
        const data = await resp.json();
        console.log(formData);
        console.log(chalk.green('deploy success, this pages at OSRC addr:'));
        
        console.log(chalk.blue(chalk.underline("https://os.osrc.com/"+username+"/projects/"+projectId+"?version="+appInfo.version+"&tab=pages&page="+data.pageId)));
        return data;
    } else {
        const data = await r.text();
        console.log(chalk.red('deploy error', r.status, data));
        process.exit(-1);
    }
}

export async function remoteCheck(appInfo) {
    const {scope, accessToken, remote} = loadConfig();
    const url = `${remote}/api/pages/check` + "?type=" + scope.type + "&scopeId=" + scope.id;
    console.log('url', url, appInfo);
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(appInfo),
    });
    if (response.status === 200) {
        const data = await response.json();
        console.log('remoteCheck', data);
        return data;
    }
    if (response.status === 401) {
        console.log(chalk.red('please login again'), chalk.green('osrc login'));
        process.exit(-1);
    }
}

export async function getUserInfo() {
    const config = loadConfig();
    // const { accessToken, remote } = loadConfig();
    const url = `${config.remote}/api/users`;
    const response = await fetch(url, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${config.accessToken}`,
            'Content-Type': 'application/json',
        }
    });
    if (response.status === 200) {
        const data = await response.json();
        config.scope.type = 'user'
        config.scope.id = data.id.toString()
        saveConfig(config);
        return data;
    }
    if (response.status === 401) {
        console.log(chalk.red('please login again'), chalk.green('osrc login'));
        process.exit(-1);
    }
}

export async function getUserOrganizationAccessRole(name) {
    const config = loadConfig();
    const url = `${config.remote}/api/organizations/role?name=` + name;
    const response = await fetch(url, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${config.accessToken}`,
            'Content-Type': 'application/json',
        }
    });
    if (response.status === 200) {
        const data = await response.json();
        if (data.accessRole !== 'OWNER') {
            console.log(chalk.red('You don’t have the access to deploy'));
            process.exit(-1);
        } else {
            config.scope.type = 'organization'
            config.scope.id = data.orgId.toString()
            saveConfig(config);
        }
        return data;
    } else if (response.status === 401) {
        console.log(chalk.red('please login again'), chalk.green('osrc login'));
        process.exit(-1);
    } else {
        const data = await response.json();
        console.log(chalk.red(data.msg));
        process.exit(-1);
    }
}

export async function checkProjectInfoByName(name) {
    const {scope, accessToken, remote} = loadConfig();
    const url = `${remote}/api/projects/check?name=` + name + "&type=" + scope.type + "&scopeId=" + scope.id;
    ;
    const response = await fetch(url, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        }
    });
    if (response.status === 200) {
        const data = await response.json();
        return data;
    } else if (response.status === 401) {
        console.log(chalk.red('please login again'), chalk.green('osrc login'));
        process.exit(-1);
    } else {
        const data = await response.json();
        console.log(chalk.red(data.msg));
        process.exit(-1);
    }
}

export async function addPagesProject(name, bundleStr) {
    const {scope, accessToken, remote} = loadConfig();
    const url = `${remote}/api/projects/page-add?name=` + name + "&bundleStr=" + bundleStr + "&type=" + scope.type + "&scopeId=" + scope.id;
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        }
    });
    if (response.status === 200) {
        const data = await response.json();
        return data;
    } else if (response.status === 401) {
        console.log(chalk.red('please login again'), chalk.green('osrc login'));
        process.exit(-1);
    } else {
        const data = await response.json();
        console.log(chalk.red(data.msg));
        process.exit(-1);
    }
}


export async function addProjectsPageBundleRelations(projectId, bundleStr) {
    const {scope, accessToken, remote} = loadConfig();
    const url = `${remote}/api/projects/page-bundle?projectId=` + projectId + "&bundleStr=" + bundleStr + "&type=" + scope.type + "&scopeId=" + scope.id;
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        }
    });
    if (response.status === 200) {
        const data = await response.json();
        return data;
    } else if (response.status === 401) {
        console.log(chalk.red('please login again'), chalk.green('osrc login'));
        process.exit(-1);
    } else {
        const data = await response.json();
        console.log(chalk.red(data.msg));
        process.exit(-1);
    }
}

export async function getUserOrganizationsCount() {
    const {accessToken, remote} = loadConfig();
    const url = `${remote}/api/organizations/count`;
    const response = await fetch(url, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        }
    });
    if (response.status === 200) {
        const data = await response.text();
        if (Number(data) == 0) {
            console.log(chalk.red("Your have not join any organization!"));
            process.exit(-1);
        }
        return data;
    } else if (response.status === 401) {
        console.log(chalk.red('please login again'), chalk.green('osrc login'));
        process.exit(-1);
    } else {
        const data = await response.json();
        console.log(chalk.red(data.msg));
        process.exit(-1);
    }
}

export async function checkUserPagesDeployInfo(bundleStr) {
    const {scope, accessToken, remote} = loadConfig();
    const url = `${remote}/api/projects/pages-deploy?bundleStr=` + bundleStr + '&type=' + scope.type + '&scopeId=' + scope.id;
    const response = await fetch(url, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        }
    });
    if (response.status === 200) {
        const data = await response.json();
        return data;
    } else if (response.status === 401) {
        console.log(chalk.red('please login again'), chalk.green('osrc login'));
        process.exit(-1);
    } else {
        const data = await response.json();
        console.log(chalk.red(data.msg));
        process.exit(-1);
    }
}


export const KB = 1024;
export const MB = 1024 * KB;
export const GB = 1024 * MB;

export function formatSize(size = 0, defaultUnit = 'B') {
    if (defaultUnit === 'K') {
        size = size << 10;
    } else if (defaultUnit === 'M') {
        size = size << 20;
    }

    if (size < KB) {
        return `${size}B`;
    }
    if (size < MB) {
        const a = (size / KB).toFixed(2);
        return `${a}K`;
    }
    if (size < GB) {
        const a = (size / MB).toFixed(2);
        return `${a}M`;
    }
    const a = (size / GB).toFixed(2);
    return `${a}G`;
}
