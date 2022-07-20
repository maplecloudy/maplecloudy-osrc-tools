# maplecloudy-osrc-tools

This project is `OSRC deployment` toolkit. 

Frontend website(static site) deployment tool [osrc-cli](osrc-cli/README.md)    
Back-end app deployment tool [maplecloudy-osrc-maven-plugin](maplecloudy-osrc-maven-plugin/README.md)   


### [osrc-cli](osrc-cli/README.md)

```
npm i -g osrc-cli
```

### [maplecloudy-osrc-maven-plugin](maplecloudy-osrc-maven-plugin/README.md)
```
  <plugin>
          <groupId>com.maplecloudy.osrc</groupId>
          <artifactId>maplecloudy-osrc-maven-plugin</artifactId>
          <version>0.1.0</version>
          <executions>
              <execution>
                  <goals>
                     <goal>repackage</goal>
                     <!-- enable upload app -->
                     <goal>install-osrc-app</goal>
                     </goals>
              </execution>
          </executions>
  </plugin>
```
