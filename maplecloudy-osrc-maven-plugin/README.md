# maplecloudy-osrc-maven-plugin
## introduction
The App can have multiple main methods that execute, and we divide these main methods into two types: **service**和**task**
* service:

* task:  

We introduced **osrc annotations** to distinguish between the two types of main. Just add the annotation to the main-class.
```
service: @com.maplecloudy.osrt.app.annotation.Service 
task: @com.maplecloudy.osrt.app.annotation.Task
```
We classified Spring's default startup class as a Service type. 

Note that osrc annotations are not mandatory.

## pom setting
  ```
  <plugin>
          <groupId>com.maplecloudy.osrc</groupId>
          <artifactId>maplecloudy-osrc-maven-plugin</artifactId>
          <version>1.0.0-SNAPSHOT</version>
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


## generate xxx.jar
* add osrt annotation  
service: `@com.maplecloudy.osrt.app.annotation.Service`  
task: `@com.maplecloudy.osrt.app.annotation.Task`

* default config 

compile result: `xxx-osrc-app.jar`

## generate xxx.war
Only one main method is allowed in the code, otherwise the compilation will report an error:
`Unable to find a single main class from the following candidates xxx`

* add osrc annotation  
service: `@com.maplecloudy.osrt.app.annotation.Service`

* default  config
  

compile result: `xxx-osrc-app.war`

## execute `mvn clean install -Dinstall.osrt.skip`
The code compiles normally, but is not deployed to OSRC. 

## execute `mvn clean install`
if the token stored locally expired,need to input username and password to got the token and store.

## the login to osrc config file store as

location: `~/.osrc`
the config info store as:
```json
{
  "remote": "https://www.osrc.com",
  "username": "osrc",
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MzkwNzU3NjEsInVzZXJfbmFtZSI6Inl1YW5ibyIsImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiNVZNNVNSRzNoanRaX19YUVlFMzllQ3RRREowIiwiY2xpZW50X2lkIjoibWFwbGVjbG91ZHkiLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.jMJI8wyZhr-OEBvFdtyHPTW9bxJZjxFehSpEvqmn_Zi3kyIkvoFcwtToFz7w6M9q4ECFBuGXuo8YlLehILmfcQXM-cOP4tzpo9as8_1Jot4JD5FXQqbd3pRTEcxUKhK4QgJ7p8JKsEbjaHQDzN_9RkxjkLEW-yDpYks0DCk80Rdlo__UvQkgLaXMFAruULsxvYxTn7YvkLDG_xs4MLDv0sO9Y73Hotl1z_qjUm-yOjOus4CkjGh9XYYyL9ZTeuQ1YQFeWY-BYjT_tSjCR85SiRZZsf5Ozc9FiJCo2yX9b7JjaTlrRa_AHIOmZnXRVLUoSWvKew5hzL0M2n5aqipzsQ",
  "tokenType": "bearer"
}
```