# OSRC CLI   

The cli tool for users deploy website(static site) to [OSRC PAGES](https://www.osrc.com/explore/pages).  

## Quickly Start   

1. Install Cli via `npm` or `yarn`

```bash
npm i -g osrc-cli
# or
yarn global add osrc-cli
```
then you can get the cli `osrc`, just use it like:

```bash
osrc
Usage: osrc [command]

osrc frontend tools

Options:
  -V, --version         output the version number
  -r,--remote <remote>  config osrc deploy target, this is for developer of osrc,defalut is https://www.osrc.com,
  -h, --help            display help for command

Commands:
  info                  print config info
  login                 login osrc
  deploy                deploy built assets to osrc, default target is dist
```
- tips :  
*osrc-cli support "node": "^12.20.0 || ^14.13.1 || >=16.0.0"*
- workaround for other node version: 
*build the SPA project with your wanted node version, the switch to others node version for use osrc cli!*

2. Build Project Static Files  

3. Osrc Login  

```bash
osrc login
# input username and password, got the token and store
``` 

4. Osrc Deploy 

The deployment default static file dir is `dist`, if your project's static build output as `dist`, you can ignore the `dist` para!

```bash
# after 2.Build Project Static Files
# custom the deploy dir or use default `dist`
osrc deploy 
osrc deploy -d dist
osrc deploy --dirname dist
```

5. Success! 

You can visit the osrc.com workbench to see the project you just deployed now!   

## How to Use   

### Config Info 

```bash
# print the osrc config info of current
osrc info
```

### login

```bash
osrc login
# input username and password, got the token and store
``` 

### deploy
The deployment default static dir is `dist`, if your project's static build output as `dist`, you can ignore this para! 

```bash
# after the build finished
# custom the deploy dir or use default `dist`
osrc deploy 
osrc deploy -d dist
osrc deploy --dirname dist
```

### The login to osrc config file store as

location: `~/.osrc`
the config info store as:
```json
{
  "remote": "https://www.osrc.com",
  "username": "osrc",
  "accessToken": "3Uf3Lcdx45KmaZASPV_xxxxxxxxx",
  "tokenType": "Bearer",
  "scope": {
    "type": "user",
    "id": "xxxx"
  }
}
```

### custom osrc server address
this is for developer, the default is https://www.osrc.com/, you can ignore this cmd!

```bash
osrc -r https://www.osrc.com/
osrc --remote https://www.osrc.com/
```

## Dev Advanced

usr 'nodejs esmodule' for the osrc command tools

For Debug
`DEBUG=* nodejs scripts/osr-cli/osrc-cli.mjs login`

For dev use 'npm link' get the osrc cmd
```bash
yarn   
npm link
osrc
```
