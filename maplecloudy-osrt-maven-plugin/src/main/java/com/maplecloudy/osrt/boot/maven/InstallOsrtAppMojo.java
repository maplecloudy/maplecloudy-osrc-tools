package com.maplecloudy.osrt.boot.maven;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.maplecloudy.osrt.boot.HttpUtils;
import com.maplecloudy.osrt.model.app.Config;
import com.maplecloudy.osrt.model.basic.OsrtProjectConfig;
import com.maplecloudy.osrt.model.basic.Scope;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.transfer.project.install.ProjectInstallerRequest;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Installs the project's main artifact, and any other artifacts attached by
 * other plugins in the lifecycle, to osrt app center.
 */
@Mojo(name = "install-osrt-app", defaultPhase = LifecyclePhase.INSTALL, requiresProject = true, threadSafe = true)
@Execute(phase = LifecyclePhase.INSTALL)
public class InstallOsrtAppMojo extends AbstractMojo {

  /**
   * When building with multiple threads, reaching the last project doesn't have
   * to mean that all projects are ready to be installed
   */
  private static final AtomicInteger READYPROJECTSCOUTNER = new AtomicInteger();

  private static final List<ProjectInstallerRequest> INSTALLREQUESTS = Collections.synchronizedList(
      new ArrayList<ProjectInstallerRequest>());

  /**
   *
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
  private List<MavenProject> reactorProjects;

  @Parameter(defaultValue = "${project.build.finalName}-osrc-app", readonly = true)
  private String finalName;

  @Parameter
  private String classifier;

  /**
   * Directory containing the generated archive.
   *
   * @since 1.0.0
   */
  @Parameter(defaultValue = "${project.build.directory}", required = true)
  private File outputDirectory;

  /**
   * Whether every project should be installed during its own install-phase or
   * at the end of the multimodule build. If set to {@code true} and the build
   * fails, none of the reactor projects is installed.
   * <strong>(experimental)</strong>
   *
   * @since 2.5
   */
  @Parameter(defaultValue = "false", property = "installAtEnd")
  private boolean installAtEnd;

  /**
   * Set this to <code>true</code> to bypass artifact installation. Use this for
   * artifacts that does not need to be installed in the local repository.
   *
   * @since 2.4
   */
  @Parameter(property = "install.osrt.skip", defaultValue = "false")
  private boolean skip;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().info("Skipping install osrt app");
    } else {
      installProject();
    }
  }

  private static ObjectMapper om = new ObjectMapper();
  private Map<String,Object> userMap = new HashMap<>();
  private Map<String,Object> projectMap = new HashMap<>();
  private OsrtProjectConfig prjConfig = new OsrtProjectConfig();

  {
    om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    om.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
  }

  private void installProject()
      throws MojoFailureException, MojoExecutionException {
    try {
      Artifact artifact = project.getArtifact();
      getLog().info("install osrt Artifact:" + artifact);
      String packaging = project.getPackaging();
      File pomFile = project.getFile();

      List<Artifact> attachedArtifacts = project.getAttachedArtifacts();

      getLog().info("install osrt attachedArtifacts:" + attachedArtifacts);
      boolean isPomArtifact = "pom".equals(packaging);

      ProjectArtifactMetadata metadata;

      getLog().info("install osrt isPomArtifact:" + isPomArtifact);
      if (isPomArtifact)// app intall ingore pom Artifact
      {
        getLog().info("install osrt skip Pom Artifact!");
        return;
      }
      // else {
      //
      // if (pomFile != null) {
      // metadata = new ProjectArtifactMetadata(artifact, pomFile);
      // artifact.addMetadata(metadata);
      // }
      // }
      JarFile targerJar = null;
      PostMethod m = null;
      GetMethod mg = null;
      try {
        File target = getTargetFile(this.finalName, this.classifier,
            this.outputDirectory);
        getLog().info("install osrt file:" + target);
        HttpClient hc = new HttpClient();
        hc.getParams().setParameter("http.useragent",
            "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)");
        String osrtAppSite = System.getenv("OSRT_APP_SITE");
        String osrtAppToken = System.getenv("OSRT_APP_TOKEN");
        File osrcFile = new File(SystemUtils.getUserHome(), ".osrc");
        boolean exists = osrcFile.exists();
        Config config = null;
        if (exists) {
          config = om.readValue(osrcFile, Config.class);
        }
        if (StringUtils.isBlank(osrtAppSite)) {
          if (!ObjectUtils.isEmpty(config) && !ObjectUtils.isEmpty(
              config.getRemote())) {
            osrtAppSite = config.getRemote();
          } else {
            config = new Config();
            config.setRemote("https://www.osrc.com");
            osrtAppSite = config.getRemote();
          }
        }
        if (StringUtils.isBlank(osrtAppToken)) {
          if (!ObjectUtils.isEmpty(config) && !ObjectUtils.isEmpty(
              config.getAccessToken())) {
            osrtAppToken = config.getAccessToken();
          }
        }
        Header header = new Header("Authorization", "Bearer " + osrtAppToken);
        if (ObjectUtils.isEmpty(osrtAppToken)) {
          boolean flag = checkLoginInfo(osrtAppSite, header, hc, target);
          if (!flag) {
            return;
          }
        } else {
          // verify token
          mg = new GetMethod(osrtAppSite + "/api/users");
          mg.addRequestHeader(header);
          int mgRespStatus = hc.executeMethod(mg);
          if (mgRespStatus == 200) {
            Map map = om.readValue(mg.getResponseBody(), Map.class);
            userMap = map;
            String username = map.get("username").toString();
            getLog().info(
                "The installation will be performed under username: " + username
                    + "!");
          } else if (mgRespStatus == 401) {
            getLog().error("Token expired!please try to login!");
            boolean flag = checkLoginInfo(osrtAppSite, header, hc, target);
            if (!flag) {
              return;
            }
          } else {
            getLog().error("Failed to obtain user information! ");
            getLog().error(
                "failed with code: " + mgRespStatus + ",error message:"
                    + mg.getResponseBodyAsString());
            boolean flag = checkLoginInfo(osrtAppSite, header, hc, target);
            if (!flag) {
              return;
            }
          }
        }

        //校验.osrc/config.json文件,如果不通过,直接返回
        boolean checkProjectValid = checkProjectConfig(osrtAppSite, header);
        if (!checkProjectValid) {
          return;
        }

        targerJar = new JarFile(target);
        //app check
        JarEntry indexEntry = targerJar.getJarEntry("index.yml");
        if (indexEntry == null) {
          getLog().error("install osrt skip package not have [index.yml]!");
          return;
        }
        FilePart indexFilePart = executeAppCheck(targerJar, target, hc,
            osrtAppSite, header, indexEntry);
        if (indexFilePart == null) {
          return;
        }

        getLog().info("***********" + om.writeValueAsString(prjConfig));
        //app install
        m = new PostMethod(osrtAppSite + "/api/apps/install-app-file");
        m.addRequestHeader(header);
        StringPart stringPart = new StringPart("projectConfig",
            om.writeValueAsString(prjConfig));
        stringPart.setContentType("application/json");
        Part[] parts = {indexFilePart, new FilePart("appFile", target),
            stringPart};

        MultipartRequestEntity mre = new MultipartRequestEntity(parts,
            m.getParams());
        m.setRequestEntity(mre);

        getLog().info("begin to deploy app...");
        int respStatus = hc.executeMethod(m);
        if (respStatus == 200) {
          getLog().info("install osrt file:" + target + " sucesss!");
          getLog().info("App info: " + m.getResponseBodyAsString());
        } else {
          getLog().error(
              "install osrt file:" + target + " failed with code: " + respStatus
                  + ",error message:" + m.getResponseBodyAsString());
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        IOUtils.closeQuietly(targerJar);
        if (m != null) {
          m.releaseConnection();
        }
        if (mg != null) {
          mg.releaseConnection();
        }
      }

    } catch (Exception e) {
      throw new MojoFailureException("Exception", e);
    }

  }

  private FilePart executeAppCheck(JarFile targerJar, File target,
      HttpClient hc, String osrtAppSite, Header header, JarEntry indexEntry)
      throws IOException {
    byte[] index = IOUtils.readFully(targerJar.getInputStream(indexEntry),
        (int) indexEntry.getSize());
    ByteArrayPartSource bps = new ByteArrayPartSource("index.yml", index);
    //构建indexFilePart
    FilePart indexFilePart = new FilePart("index", bps,
        FilePart.DEFAULT_CONTENT_TYPE, StandardCharsets.UTF_8.name());
    getLog().info("start to verify app deploy enabled or not");
    PostMethod check = new PostMethod(osrtAppSite + "/api/apps/check");
    check.addRequestHeader(header);
    Part[] indexPart = {indexFilePart};
    MultipartRequestEntity mreIndex = new MultipartRequestEntity(indexPart,
        check.getParams());

    check.setRequestEntity(mreIndex);
    int code = hc.executeMethod(check);
    if (code == 200) {
      Map map = om.readValue(check.getResponseBody(), Map.class);
      Integer innerCode = (Integer) map.get("code");
      if (200 == innerCode) {
        getLog().info(map.get("msg").toString());
      } else {
        getLog().error(map.get("msg") + "\nApp info: " + om.writeValueAsString(
            map.get("data")));
        return null;
      }
    } else {
      getLog().error(
          "install osrt file:" + target + " failed with code: " + code
              + ",error message:" + check.getResponseBodyAsString());
      return null;
    }
    return indexFilePart;
  }

  protected File getTargetFile(String finalName, String classifier,
      File targetDirectory) {
    String classifierSuffix = (classifier != null) ? classifier.trim() : "";
    if (!classifierSuffix.isEmpty() && !classifierSuffix.startsWith("-")) {
      classifierSuffix = "-" + classifierSuffix;
    }
    if (!targetDirectory.exists()) {
      targetDirectory.mkdirs();
    }
    return new File(targetDirectory,
        finalName + classifierSuffix + "." + this.project.getArtifact()
            .getArtifactHandler().getExtension());
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

  public boolean checkLoginInfo(String osrtAppSite, Header header,
      HttpClient hc, File target) throws Exception {
    Scanner sc = new Scanner(System.in);
    PostMethod m = null;
    String username = null;
    while (true) {
      getLog().info("please login (y/n)? ");
      String yn = sc.nextLine();
      if ("y".equalsIgnoreCase(yn) || "yes".equalsIgnoreCase(yn)) {
        getLog().info("please input the username: ");
        username = sc.nextLine();
        getLog().info("please input the password: ");
        String password = sc.nextLine();
        m = new PostMethod(osrtAppSite + "/api/users/signin");
        Map<String,String> mapPara = Maps.newHashMap();
        mapPara.put("username", username);
        mapPara.put("password", password);
        String userInfoStr = om.writeValueAsString(mapPara);
        RequestEntity entity = new StringRequestEntity(userInfoStr,
            "application/json", "UTF-8");
        m.setRequestEntity(entity);
        int code = hc.executeMethod(m);
        if (code == 200) {
          getLog().info("login successfully!");
          getLog().info(
              "The installation will be performed under username: " + username
                  + "!");
          Map tokenMap = om.readValue(m.getResponseBody(), Map.class);
          String token = tokenMap.get("accessToken").toString();
          Config config = new Config();
          config.setAccessToken(token);
          config.setUsername(username);
          config.setTokenType(tokenMap.get("tokenType").toString());
          config.setRemote(osrtAppSite);
          File osrcFile = new File(SystemUtils.getUserHome(), ".osrc");
          om.writeValue(osrcFile, config);
          header.setName("Authorization");
          header.setValue("Bearer " + token);
          GetMethod mg = new GetMethod(osrtAppSite + "/api/users");
          mg.addRequestHeader(header);
          int mgRespStatus = hc.executeMethod(mg);
          if (mgRespStatus == 200) {
            Map map = om.readValue(mg.getResponseBody(), Map.class);
            userMap = map;
            username = map.get("username").toString();
            getLog().info(
                "The installation will be performed under username: " + username
                    + "!");
          }
          break;
        } else if (code == 401) {
          getLog().error("Token expired!please try to login!");
        } else {
          getLog().error(
              "install osrt file:" + target + " failed with code: " + code
                  + ",error message:" + m.getResponseBodyAsString());
        }
      } else if ("n".equalsIgnoreCase(yn) || "no".equalsIgnoreCase(yn)) {
        getLog().info("Skipping install osrt app");
        return false;
      } else {
        continue;
      }
    }
    return true;
  }

  public boolean checkProjectConfig(String osrtAppSite, Header header)
      throws Exception {
    File file = new File(".osrc/config.json");
    Scanner sc = new Scanner(System.in);
    if (!file.exists()) {
      if (file.getParentFile() != null && !file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      file.createNewFile();
      //1. 确定scope
      Scope scope = initScope(sc, osrtAppSite, header);
      //2. 确定projectId
      String projectId = initProject(sc, scope, osrtAppSite, header);
      prjConfig.setProjectId(projectId);
      prjConfig.setScope(scope);
      om.writeValue(file, prjConfig);

      return true;
      //config.json文件存在
    } else {
      //2. 确定project
      boolean configValid = checkProjectJsonFileValid(osrtAppSite, file,
          header);
      if (!configValid) {
        //1. 确定scope
        Scope scope = initScope(sc, osrtAppSite, header);
        //2. 确定projectId
        String projectId = initProject(sc, scope, osrtAppSite, header);
        prjConfig.setProjectId(projectId);
        prjConfig.setScope(scope);
        om.writeValue(file, prjConfig);
        return true;
      } else {
        String ownerName = projectMap.get("ownerName").toString();
        String prjName = projectMap.get("name").toString();
        getLog().info(
            "Found project “" + ownerName + "/" + prjName + "”. Link to "
                + "it? [Y/n]");
        String yn = sc.nextLine();
        while (true) {
          if ("y".equalsIgnoreCase(yn)) {
            prjConfig = om.readValue(file, OsrtProjectConfig.class);
            return true;
          } else if ("n".equalsIgnoreCase(yn)) {
            //1. 确定scope
            Scope scope = initScope(sc, osrtAppSite, header);
            //2. 确定projectId
            String projectId = initProject(sc, scope, osrtAppSite, header);
            prjConfig.setProjectId(projectId);
            prjConfig.setScope(scope);
            om.writeValue(file, prjConfig);
            return true;
          } else {
            continue;
          }
        }
      }
    }
  }

  private boolean checkProjectJsonFileValid(String osrtAppSite, File file,
      Header header) throws IOException {
    //校验文件内容
    OsrtProjectConfig checkPrjConfig = om.readValue(file,
        OsrtProjectConfig.class);
    if (ObjectUtils.isEmpty(checkPrjConfig) || ObjectUtils.isEmpty(
        checkPrjConfig.getProjectId())) {
      return false;
    }
    Scope scope = checkPrjConfig.getScope();
    String scopeType = scope.getType();
    String scopeId = scope.getId();
    if (ObjectUtils.isEmpty(scope) || ObjectUtils.isEmpty(scopeId)
        || ObjectUtils.isEmpty(scopeType)) {
      return false;
    }

    Map<String,Object> maps = new HashMap();
    maps.put("id", checkPrjConfig.getProjectId());
    maps.put("scopeId", Integer.valueOf(scope.getId()));
    maps.put("type", scopeType);
    String projectStr = HttpUtils.doGet(osrtAppSite + "/api/projects/check",
        maps, header);
    if (ObjectUtils.isEmpty(projectStr)) {
      getLog().error(
          "Your Project was either deleted, transferred to a new Team, or you don’t have access to it anymore");
      return false;
    }
    projectMap = om.readValue(projectStr, Map.class);
    //Integer ownerId = Integer.valueOf(projectMap.get("ownerId").toString());
    //Integer ownerType = Integer.valueOf(projectMap.get("ownerType").toString());
    //if (ownerType == 0) {
    //  if (!StringUtils.equals(scopeType, "user")) {
    //    return false;
    //  } else {
    //    //校验所属者
    //    if (!ownerId.equals(Integer.valueOf(scopeId))) {
    //      return false;
    //    }
    //  }
    //} else if (ownerType == 1) {
    //  if (!StringUtils.equals(scopeType, "team")) {
    //    return false;
    //  } else {
    //    //校验所属者
    //    if (!ownerId.equals(Integer.valueOf(scopeId))) {
    //      return false;
    //    }
    //    //todo type为team时,上传者的team权限校验
    //    Map<String,Object> teamCheckPara = new HashMap();
    //    teamCheckPara.put("id", ownerId);
    //    String respStr = HttpUtils.doGet(osrtAppSite + "/api/users/team/access",
    //        teamCheckPara, header);
    //    Map map = om.readValue(respStr, Map.class);
    //    if (200 != (Integer) map.get("code")) {
    //      getLog().error(
    //          "Your Project was either deleted, transferred to a new Team, or you don’t have access to it anymore");
    //      return false;
    //    }
    //  }
    //}
    return true;
  }

  private Scope initScope(Scanner sc, String osrtAppSite, Header header)
      throws JsonProcessingException {
    Scope scope = new Scope();
    while (true) {
      getLog().info("Which scope do you want to deploy to? \n 1 "
          + "personal account \n 2 teams" + " ");
      String type = sc.nextLine();

      if (StringUtils.equals("1", type)) {
        scope.setType("user");
        scope.setId(userMap.get("id").toString());
        break;
      } else {
        //查询返回当前的user所在的有上传权限的team的name
        String teamsStr = HttpUtils.doGet(osrtAppSite + "/api/users/teams",
            null, header);
        getLog().error("*********"+teamsStr);
       if(ObjectUtils.isEmpty(teamsStr)) {
          getLog().error("Your have not join any team,or you don’t have access"
              + " to the deploy access anymore ");
          continue;
        } else {
         List<Map<String,Object>> teamMapList = om.readValue(teamsStr,
             new TypeReference<List<Map<String,Object>>>() {
             });
          List<String> tnames = Lists.newArrayList();
          Map<String,Object> teamMap = new HashMap<>();
          teamMapList.forEach(m -> {
            String name = m.get("teamName").toString();
            tnames.add(name);
            teamMap.put(name, m.get("teamId"));
          });
          //控制台展示所有的team name
         String joinWith = StringUtils.joinWith("\n", tnames);
         getLog().info("Your teams: \n"+joinWith);
          //tnames.forEach(m -> {
          //  getLog().info( " "+m + "\n");
          //});
          while (true) {
            getLog().info("please input the team name: ");
            String tname = sc.nextLine();
            if (!tnames.contains(tname)) {
              getLog().error("name not exists!");
              continue;
            } else {
              scope.setType("team");
              scope.setId(teamMap.get(tname).toString());
              break;
            }
          }
          break;
        }
      }
    }
    return scope;
  }

  private String initProject(Scanner sc, Scope scope, String osrtAppSite,
      Header header) throws JsonProcessingException {
    String projectId = null;
    while (true) {
      getLog().info("Link to existing project? [Y/n]");
      String yn = sc.nextLine();
      Map<String,Object> nameMap = new HashMap<>();
      //绑定到已存在的project
      if ("y".equalsIgnoreCase(yn)) {
        getLog().info("What’s the name of your existing project?");
        String projectName = sc.nextLine();
        nameMap.put("name", projectName);
        nameMap.put("scopeId", Integer.valueOf(scope.getId()));
        nameMap.put("type", scope.getType());
        String prjStr = HttpUtils.doGet(osrtAppSite + "/api/projects/check",
            nameMap, header);
        if (StringUtils.isNotBlank(prjStr)) {
          projectMap = om.readValue(prjStr, Map.class);
          projectId = projectMap.get("id").toString();
          break;
        } else {
          getLog().warn("Project not exists,or you have no access!");
          continue;
        }
        //新增
      } else if ("n".equalsIgnoreCase(yn)) {
        getLog().info("What’s your project’s name?");
        String projectName = sc.nextLine();
        nameMap.put("name", projectName);
        nameMap.put("scopeId", Integer.valueOf(scope.getId()));
        nameMap.put("type", scope.getType());
        String prjStr = HttpUtils.doPost(osrtAppSite + "/api/projects/add",
            nameMap, header);
        if (StringUtils.isNotBlank(prjStr)) {
          projectMap = om.readValue(prjStr, Map.class);
          projectId = projectMap.get("id").toString();
          break;
        } else {
          getLog().info("Project already exists! ");
          continue;
        }
      } else {
        getLog().info("Wrong input value.");
        continue;
      }
    }
    return projectId;
  }

  public static void main(String[] args) throws IOException {
    String osrtAppSite = "http://localhost:16891";
    String osrtAppToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NTAwNDgyMDMsInVzZXJfbmFtZSI6Im9zcmMiLCJhdXRob3JpdGllcyI6WyJ1c2VyIl0sImp0aSI6InIzWE5IbF9QaFNsZml6Znd2NWdDVTZOaUVJOCIsImNsaWVudF9pZCI6Im1hcGxlY2xvdWR5Iiwic2NvcGUiOlsicmVhZCIsIndyaXRlIl19.AxB_QPHs5-FQ-au5U3nmKEjzt8yfsM5M4Jc6n55o2dMv6AslsCUI-k_XAaf0ahsxDFM4eflYH0k0dQFsRg79Tv-dixiX9Xh_U28VOpYC6Cg8edEY9BZVOOtwG5Y_y57K6m8Esf6_4DqcXGz_miV1N0HMdykDaPQz6o2cVdQFtQWSPVc4C4jRjKzFKvjCInH2GrqKMkZRsEySpQUwA0zI-_tklgmYerefyOQ1ErVNGeDjT3BX9WZFpAp4mUXvcRG0-xcjqo0zwbrpvqhWCMW2k4hK7qHlSTTVkst9MCekQTxwAesLMMERwCRpZLoXLhntFva1pOPpjP1wWCp8hHdSkA";
    HttpClient hc = new HttpClient();
    hc.getParams().setParameter("http.useragent",
        "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)");
    Header header = new Header("Authorization", "Bearer " + osrtAppToken);
    GetMethod check = new GetMethod(osrtAppSite + "/api/projects/check");

    HttpMethodParams methodParams = new HttpMethodParams();
    methodParams.setParameter("id", "project_765260450064130048");
    check.setParams(methodParams);
    NameValuePair valuePair = new NameValuePair("id",
        "project_765260450064130048");
    check.setQueryString(new NameValuePair[] {valuePair});

    check.addRequestHeader(header);

    //int code = hc.executeMethod(check);
    //
    //System.out.println(check.getResponseBodyAsString());

    HashMap<String,Object> stringHashMap = new HashMap<>();
    stringHashMap.put("id", "project_765260450064130048");


    PostMethod httpPost = new PostMethod(
        osrtAppSite + "/api/apps/install-app-file");
    httpPost.addRequestHeader(header);
    StringPart stringPart = new StringPart("projectConfig",
        "{\"projectId\":\"project_775024354616819712\","
            + "\"scope\":{\"type\":\"user\",\"id\":\"2\"}}");
    stringPart.setContentType("application/json");
    FilePart filePart = new FilePart("index",
        new File("C:\\Users\\Parker\\Desktop\\index.yml"),
        FilePart.DEFAULT_CONTENT_TYPE, StandardCharsets.UTF_8.name());
    Part[] parts = {filePart, stringPart};
    MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts,
        new HttpMethodParams());
    httpPost.setRequestEntity(requestEntity);
    hc.executeMethod(httpPost);
    System.out.println(httpPost.getResponseBodyAsString());


    List<Map<String,Object>> teamMapList = om.readValue("[]",
        new TypeReference<List<Map<String,Object>>>() {
        });

    System.out.println(teamMapList);

  }
}
