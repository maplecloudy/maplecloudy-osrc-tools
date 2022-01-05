import commander from 'commander';
import inquirer from 'inquirer';
import { login } from './utils.mjs';
import Debug from 'debug';

const debug = Debug('osrc:login');

commander
  .action(async function () {
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
  })
  .parse(process.argv);
