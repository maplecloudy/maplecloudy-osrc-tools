#! /usr/bin/env node
import commander from 'commander';
import { loadConfig, saveConfig } from './utils.mjs';

commander
  .version('v1.0.0')
  .usage(`[command]`)
  .description('osrc frontend tools');

commander.option('-r,--remote <remote>', 'config osrc').action(function (args) {
  if (args.remote) {
    const config = loadConfig();
    config.remote = args.remote;
    saveConfig(config);
  } else {
    commander.outputHelp();
  }
});

commander.command('info', 'print configs');

commander.command('login', 'login osrc');

commander.command('deploy', 'deploy built assets to osrc, default target is dist');

commander.parse(process.argv);
