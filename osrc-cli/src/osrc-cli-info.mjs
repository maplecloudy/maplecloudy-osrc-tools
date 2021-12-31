import commander from 'commander';
import { loadConfig } from './utils.mjs';
import chalk from 'chalk';
import Debug from 'debug';

const debug = Debug('mcli:login');

commander
  .action(async function () {
    debug('info.action');
    const config = loadConfig();
    console.log(chalk.green('remote'), config.remote);
    console.log(chalk.green('username'), config.username);
  })
  .parse(process.argv);
