# osrc-cli
one cli tools for users deploy SPA to osrc Pages.
## for users deploy SPA to osrc Pages
```bash
npm i -g osrc-cli
```
then you can get the cli 'osrc', just use it like:
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
## the dev tech

usr 'nodejs esmodule' for the osrc command tools

for debug
`DEBUG=* nodejs scripts/osr-cli/osrc-cli.mjs login`

for dev use 'npm link' get the osrc cmd
```bash
npm link
osrc
```
## specific the remote osrc addr for deploy
this is for devleper, the default is https://www.osrc.com/, you can ignore this cmd!
```bash
osrc -r https://www.osrc.com/
osrc --remote https://www.osrc.com/
```

## login

```bash
osrc login
# input username and password, got the token and store
``` 

## deploy
the deploy dir defalut as 'dist', if your project build output as 'dist',you can ignore this para!
```bash
# after the build finishedd
# specific the deploy dir
osrc deploy -d dist
osrc deploy --dirname dist
```

##  print the osrc config info of current
```bash
osrc info
```

## the login to osrc config file store as

location: `~/.osrc`
the config info store as:
```json
{
  "remote": "https://www.osrc.com",
  "username": "yuanbo",
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MzkwNzU3NjEsInVzZXJfbmFtZSI6Inl1YW5ibyIsImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiNVZNNVNSRzNoanRaX19YUVlFMzllQ3RRREowIiwiY2xpZW50X2lkIjoibWFwbGVjbG91ZHkiLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.jMJI8wyZhr-OEBvFdtyHPTW9bxJZjxFehSpEvqmn_Zi3kyIkvoFcwtToFz7w6M9q4ECFBuGXuo8YlLehILmfcQXM-cOP4tzpo9as8_1Jot4JD5FXQqbd3pRTEcxUKhK4QgJ7p8JKsEbjaHQDzN_9RkxjkLEW-yDpYks0DCk80Rdlo__UvQkgLaXMFAruULsxvYxTn7YvkLDG_xs4MLDv0sO9Y73Hotl1z_qjUm-yOjOus4CkjGh9XYYyL9ZTeuQ1YQFeWY-BYjT_tSjCR85SiRZZsf5Ozc9FiJCo2yX9b7JjaTlrRa_AHIOmZnXRVLUoSWvKew5hzL0M2n5aqipzsQ",
  "tokenType": "bearer"
}
```
