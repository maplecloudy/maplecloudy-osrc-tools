import path from 'path';
import fse from 'fs-extra';
import fetch from 'node-fetch';
import chalk from 'chalk';
import Debug from 'debug';
const debug = Debug('osrc-cli:utils');

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
  } catch (e) {}
  if(!config.remote){
    config.remote = 'https://www.osrc.com';
  }
  if(!config.accessToken){
    config.accessToken = process.env.OSRT_APP_TOKEN || undefined;
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
    name,
    version,
    author,
    contributors,
    homepage,
    license,
    topics: keywords,
    repository,
    description,
    main,
  };
  const entries = Object.entries(infos).filter(([n, v]) => !!v);
  return Object.fromEntries(entries);
}

export async function login(username, password) {
  const config = loadConfig();
  debug('config', config);
  const response = await fetch(`${config.remote}/api/accounts/signin`, {
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
  }
}

export async function deployBundle(formData) {
  const { accessToken, remote } = loadConfig();
  const url = `${remote}/api/pages/upload`;
  const r = await fetch(url, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
    body: formData,
  });
  if (r.status === 200) {
    const data = await r.json();
    return data;
  } else {
    const data = await r.text();
    console.log(chalk.red('deploy error', r.status, data));
    process.exit(-1);
  }
}

export async function remoteCheck(appInfo) {
  const { accessToken, remote } = loadConfig();
  const url = `${remote}/api/pages/check`;
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
    console.log(chalk.red('please login again'), chalk.green('osrc-cli login'));
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
