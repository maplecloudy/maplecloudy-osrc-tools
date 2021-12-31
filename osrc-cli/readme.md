# mcli

## 开发

使用nodejs esmodule方式开发

开发时 
`DEBUG=* nodejs scripts/mcli/mcli.mjs login`

## 配置远程服务地址

```bash
osrc-cli -r https://www.osrc.com/
osrc-cli --remote https://www.osrc.com/
```

## 登录

```bash
osrc-cli login
# 进入交互模式, 输入用户名/密码, 取回token后, 保存到本地
``` 

## 打包发布

```bash
# 本地开发构建完成后
# 指定发布目录
osrc-cli upload -d dist
osrc-cli upload --dirname dist
# 检索package.json中name/version等必要信息
# 将dirname目录中文件压缩成.tar.gz格式后, 上传到服务器
```

## 查看配置 TODO
```bash
osrc-cli info
```

## 本地配置的存储

位置: `~/.osrc`

```json
{
  "remote": "http://192.168.8.103:16891",
  "username": "yuanbo",
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MzkwNzU3NjEsInVzZXJfbmFtZSI6Inl1YW5ibyIsImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiNVZNNVNSRzNoanRaX19YUVlFMzllQ3RRREowIiwiY2xpZW50X2lkIjoibWFwbGVjbG91ZHkiLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.jMJI8wyZhr-OEBvFdtyHPTW9bxJZjxFehSpEvqmn_Zi3kyIkvoFcwtToFz7w6M9q4ECFBuGXuo8YlLehILmfcQXM-cOP4tzpo9as8_1Jot4JD5FXQqbd3pRTEcxUKhK4QgJ7p8JKsEbjaHQDzN_9RkxjkLEW-yDpYks0DCk80Rdlo__UvQkgLaXMFAruULsxvYxTn7YvkLDG_xs4MLDv0sO9Y73Hotl1z_qjUm-yOjOus4CkjGh9XYYyL9ZTeuQ1YQFeWY-BYjT_tSjCR85SiRZZsf5Ozc9FiJCo2yX9b7JjaTlrRa_AHIOmZnXRVLUoSWvKew5hzL0M2n5aqipzsQ",
  "tokenType": "bearer"
}
```