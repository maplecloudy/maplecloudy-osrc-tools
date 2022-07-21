# maplecloudy-osrc-maven-plugin

## Introduce      

The App can have multiple main methods that execute, and we divide these main methods into two types: **service**和**task**

We introduced **osrc annotations** to distinguish between the two types of main. Just add the annotation to the main-class.
```
service: @com.maplecloudy.osrc.app.annotation.Service 
task: @com.maplecloudy.osrc.app.annotation.Task
```
We classified Spring's default startup class as a Service type. 

Note that osrc annotations are not mandatory.

## Quickly Start   

### Config pom setting
  ```
  <plugin>
          <groupId>com.maplecloudy.osrc</groupId>
          <artifactId>maplecloudy-osrc-maven-plugin</artifactId>
          <version>${lastest-version}</version>
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


### Generate xxx.jar   

* use osrc annotation endpoint (optional) 

service: `@com.maplecloudy.osrc.app.annotation.Service`  
task: `@com.maplecloudy.osrc.app.annotation.Task`

* use `@SpringBootApplication` default endpoint 

`org.springframework.boot.autoconfigure.SpringBootApplication`


### Execute `mvn clean install -Dinstall.osrc.skip`
The code compiles normally, but is not deployed to OSRC. 

### Execute `mvn clean install`   
This command will deploy app to OSRC.    
If you don't have an osrc account before, you need to register in [OSRC](https://www.osrc.com) for one.   
If the token stored locally expired, need to input username and password to get the token and store.   

### The login to osrc config file store as

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