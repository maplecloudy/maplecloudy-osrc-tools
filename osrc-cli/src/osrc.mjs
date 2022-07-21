#! /usr/bin/env node
import commander from "commander";
import {loadConfig, saveConfig} from "./utils.mjs";

import {readFile} from 'fs/promises';

// read package.json
const packageJson = JSON.parse(
    await readFile(
        new URL('../package.json', import.meta.url)
    )
);

(async function () {
    // use package.json "version" value
    commander
        .version(packageJson.version)
        .usage(`[command]`)
        .description("osrc frontend tools");

    commander
        .option(
            "-r,--remote <remote>",
            "config osrc deploy target, this is for developer of osrc,default is https://www.osrc.com,"
        )
        .action(function (args) {
            if (args.remote) {
                const config = loadConfig();
                config.remote = args.remote;
                saveConfig(config);
            } else {
                commander.outputHelp();
            }
        });

    commander.command("info", "print config info");

    commander.command("login", "login osrc");

  commander.command(
    "deploy",
    "deploy built assets to osrc, default target is dist"
  );

    commander.parse(process.argv);

})()

