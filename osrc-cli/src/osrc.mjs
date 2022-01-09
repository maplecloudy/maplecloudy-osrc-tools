import commander from "commander";
import fse from 'fs-extra'
import path from 'path'
import { loadConfig, saveConfig } from "./utils.mjs";


const __dirname = path.resolve();

(async function () {
  const packageContent = fse.readFileSync(
    path.join(__dirname,'package.json'),
    'utf-8',
  );
  const packageJson = JSON.parse(packageContent);
  const version = packageJson.version;


  commander
    .version(version)
    .usage(`[command]`)
    .description("osrc frontend tools");

  commander
    .option(
      "-r,--remote <remote>",
      "config osrc deploy target, this is for developer of osrc,defalut is https://www.osrc.com,"
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

