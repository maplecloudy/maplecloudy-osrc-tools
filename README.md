# maplecloudy-osrt-tools

This project is an `OSRC deployment` toolkit

Currently we have developed front-end app deployment tool 
[osrc-cli](https://github.com/maplecloudy/maplecloudy-osrt-tools/tree/master/osrc-cli#readme) and back-end app deployment tool [maplecloudy-osrt-maven-plugin](https://github.com/maplecloudy/maplecloudy-osrt-tools/tree/master/maplecloudy-osrt-maven-plugin#readme)


[osrc-cli](https://github.com/maplecloudy/maplecloudy-osrt-tools/tree/master/osrc-cli#readme)
```
npm i -g osrc-cli
```
[maplecloudy-osrt-maven-plugin](https://github.com/maplecloudy/maplecloudy-osrt-tools/tree/master/maplecloudy-osrt-maven-plugin#readme)
```
  <plugin>
          <groupId>com.maplecloudy.osrt</groupId>
          <artifactId>maplecloudy-osrt-maven-plugin</artifactId>
          <version>1.0.0-SNAPSHOT</version>
          <executions>
              <execution>
                  <goals>
                     <goal>repackage</goal>
                     <goal>install-osrt-app</goal>
                     </goals>
              </execution>
          </executions>
  </plugin>
```
