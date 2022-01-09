#! /usr/bin/env node
import chalk from 'chalk';

//node-fetch的node版本要求 "node": "^12.20.0 || ^14.13.1 || >=16.0.0"
const currentNodeVersion = process.versions.node;
const versionArr = currentNodeVersion.split('.');
const major = versionArr[0];
const second = versionArr[1];
const third = versionArr[2];
if (!(major>15 || (major==14 && second>12 && third>0) || (major==12 && second>19))) {
  console.error(
    chalk.red(
      `You are running Node v${currentNodeVersion} \nosrc-cli requires Node "^12.20.0 || ^14.13.1 || >=16.0.0".\nPlease update your version of Node`
    )
  );
  process.exit(1);
}

import('./osrc.mjs')
