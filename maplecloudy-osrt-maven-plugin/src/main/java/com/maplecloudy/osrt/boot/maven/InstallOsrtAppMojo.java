package com.maplecloudy.osrt.boot.maven;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Maps;
import com.maplecloudy.osrt.boot.HttpUtils;
import com.maplecloudy.osrt.model.app.App;
import com.maplecloudy.osrt.model.app.Config;
import com.maplecloudy.osrt.model.basic.Scope;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
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
import org.checkerframework.checker.nullness.qual.Nullable;
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

  {
    om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    om.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
  }

  private App app = new App();

  private Config config = new Config();

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
        Header header = new Header();
        Scanner sc = new Scanner(System.in);
        File osrcFile = new File(SystemUtils.getUserHome(), ".osrc");
        boolean osrcFileValid = checkOsrcFileValid(osrcFile, header, sc);
        if (!osrcFileValid) {
          return;
        }
        targerJar = new JarFile(target);
        //app check
        JarEntry indexEntry = targerJar.getJarEntry("index.yml");
        if (indexEntry == null) {
          getLog().error("install osrt skip! Package not have [index.yml]!");
          return;
        }

        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        yaml.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        yaml.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        yaml.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JsonNode jsonNode = yaml.readTree(targerJar.getInputStream(indexEntry));
        app = om.convertValue(jsonNode, App.class);
        //todo 根据bundle信息,判断project是新增还是update

        Map<String,Object> bundleMap = Maps.newHashMap();
        bundleMap.put("version", app.getVersion());
        bundleMap.put("bundleStr", getBundleStr(app.getBundle()));
        bundleMap.put("type", config.getScope().getType());
        bundleMap.put("scopeId", Integer.valueOf(config.getScope().getId()));
        boolean checkPrInfoFlag = true;
        while (checkPrInfoFlag) {
          getLog().info("Check project info......");
          String prBundlStr = HttpUtils.doGet(
              config.getRemote() + "/api/projects/app-deploy", bundleMap,
              header);
          if (ObjectUtils.isEmpty(prBundlStr)) {
            getLog().error("Got project bundle data error!");
            return;
          } else {
            projectMap = om.readValue(prBundlStr, Map.class);
          }
          boolean exist = Boolean.valueOf(projectMap.get("exist").toString());
          if (exist) {
            //todo 昵称还是username? name还是displayName
            String ownerName = projectMap.get("ownerName").toString();
            String prjName = projectMap.get("name").toString();
            boolean linkFlag = true;
            while (linkFlag) {
              getLog().info(
                  "Found project “" + ownerName + "/" + prjName + "”. Link to "
                      + "it? [Y/n]");
              String yn = sc.nextLine();
              if ("y".equalsIgnoreCase(yn)) {
                checkPrInfoFlag = false;
                linkFlag = false;
              } else if ("n".equalsIgnoreCase(yn)) {
                //1. 确定scope
                //Scope scope = initScope(sc, config.getRemote(), header);
                //config.setScope(scope);
                initProject(sc, config.getRemote(), header);
                linkFlag = false;
              } else {
                continue;
              }
            }
          } else {
            //todo add project
            getLog().info("Project init ......");
            initProject(sc, config.getRemote(), header);
            checkPrInfoFlag = false;
          }
        }

        FilePart indexFilePart = executeAppCheck(targerJar, target, hc,
            config.getRemote(), header, indexEntry);
        if (indexFilePart == null) {
          return;
        }

        //app install
        m = new PostMethod(
            config.getRemote() + "/api/apps/install-app-file" + "?projectId="
                + projectMap.get("projectId").toString());
        m.addRequestHeader(header);
        Part[] parts = {indexFilePart, new FilePart("appFile", target)};
        //StringPart stringPart = new StringPart("projectConfig",
        //    om.writeValueAsString(prjConfig));
        //stringPart.setContentType("application/json");
        //Part[] parts = {indexFilePart, new FilePart("appFile", target),
        //    stringPart};

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

  /**
   * 校验osrcFile的内容
   * 1. 如果.osrc不存在,则初始化登录信息和scope信息,并创建.osrc文件
   * 2. 如果.osrc文件存在,则校验文件中的登录信息和scope信息
   *
   * @param osrcFile
   * @param header
   * @param sc
   * @return
   * @throws Exception
   */
  private boolean checkOsrcFileValid(File osrcFile, Header header, Scanner sc)
      throws Exception {
    //1.初始化.osrc文件
    String osrtAppSite = null;
    if (!osrcFile.exists()) {
      //osrtAppSite = "https://www.osrc.com";
      osrtAppSite = "https://www.maplecloudy.com";
      boolean initLoginInfoSuccess = initLoginInfoSuccess(config, osrtAppSite,
          header, sc);
      if (!initLoginInfoSuccess) {
        return false;
      }
      Scope scope = initScope(sc, osrtAppSite, header);
      config.setScope(scope);
      om.writeValue(osrcFile, config);
      return true;
    } else {
      //2. 校验.osrc文件内容
      Config osrcConfig = om.readValue(osrcFile, Config.class);
      if (ObjectUtils.isEmpty(osrcConfig.getRemote())) {
        //osrtAppSite = "https://www.osrc.com";
        osrtAppSite = "https://www.maplecloudy.com";
        osrcConfig.setRemote(osrtAppSite);
      }
      //校验logininfo
      if (ObjectUtils.isEmpty(osrcConfig) || ObjectUtils.isEmpty(
          osrcConfig.getRemote()) || ObjectUtils.isEmpty(
          osrcConfig.getAccessToken()) || ObjectUtils.isEmpty(
          osrcConfig.getUsername())) {
        boolean initLoginInfoSuccess = initLoginInfoSuccess(osrcConfig,
            osrcConfig.getRemote(), header, sc);
        if (!initLoginInfoSuccess) {
          return false;
        } else {
          om.writeValue(osrcFile, osrcConfig);
        }
      } else {
        header.setName("Authorization");
        header.setValue("Bearer " + osrcConfig.getAccessToken());
        boolean checkLoginTokenValid = checkLoginTokenValid(
            osrcConfig.getRemote(), header);
        if (!checkLoginTokenValid) {
          boolean initLoginInfoSuccess = initLoginInfoSuccess(osrcConfig,
              osrcConfig.getRemote(), header, sc);
          if (!initLoginInfoSuccess) {
            return false;
          } else {
            om.writeValue(osrcFile, osrcConfig);
          }
        }
      }
      //校验scope
      Scope scope = osrcConfig.getScope();
      if (ObjectUtils.isEmpty(scope) || ObjectUtils.isEmpty(scope.getId())
          || ObjectUtils.isEmpty(scope.getType())) {
        scope = initScope(sc, osrcConfig.getRemote(), header);
        osrcConfig.setScope(scope);
        om.writeValue(osrcFile, osrcConfig);
      } else {
        boolean checkScopeValid = checkScopeValid(osrcConfig.getRemote(), scope,
            header, sc);
        if (!checkScopeValid) {
          scope = initScope(sc, osrcConfig.getRemote(), header);
          osrcConfig.setScope(scope);
          om.writeValue(osrcFile, osrcConfig);
        }
      }
      config = osrcConfig;
      return true;
    }
  }

  private boolean checkScopeValid(String osrtAppSite, Scope scope,
      Header header, Scanner sc) throws JsonProcessingException {
    Map<String,Object> maps = new HashMap();
    maps.put("scopeId", Integer.valueOf(scope.getId()));
    maps.put("type", scope.getType());

    String accessStr = HttpUtils.doGet(osrtAppSite + "/api/users/deploy/access",
        maps, header);
    if (ObjectUtils.isEmpty(accessStr)) {
      getLog().error("Scope data error! Please init the scope data again!");
      return false;
    }
    return true;
  }

  private boolean checkLoginTokenValid(String osrtAppSite, Header header)
      throws JsonProcessingException {
    String userInfoStr = HttpUtils.doGet(osrtAppSite + "/api/users", null,
        header);
    if (ObjectUtils.isEmpty(userInfoStr)) {
      getLog().error("Token expired or invalid!please try to login again!");
      return false;
    } else {
      userMap = om.readValue(userInfoStr, Map.class);
      return true;
    }
  }

  private boolean initLoginInfoSuccess(Config config, String osrtAppSite,
      Header header, Scanner sc) throws JsonProcessingException {
    String username = null;
    while (true) {
      getLog().info("please login (y/n)? ");
      String yn = sc.nextLine();
      if ("y".equalsIgnoreCase(yn) || "yes".equalsIgnoreCase(yn)) {
        getLog().info("please input the username: ");
        username = sc.nextLine();
        getLog().info("please input the password: ");
        String password = sc.nextLine();
        Map<String,Object> mapPara = Maps.newHashMap();
        mapPara.put("username", username);
        mapPara.put("password", password);
        String userTokenStr = HttpUtils.doPost(
            osrtAppSite + "/api/users/signin", om.writeValueAsString(mapPara),
            "application/json", "UTF-8");

        getLog().info("****" + userTokenStr);
        if (StringUtils.isNotBlank(userTokenStr)) {
          getLog().info("login successfully!");
          getLog().info(
              "The installation will be performed under username: " + username
                  + "!");
          Map tokenMap = om.readValue(userTokenStr, Map.class);
          String token = tokenMap.get("accessToken").toString();
          config.setAccessToken(token);
          config.setUsername(username);
          config.setTokenType(tokenMap.get("tokenType").toString());
          config.setRemote(osrtAppSite);
          header.setName("Authorization");
          header.setValue("Bearer " + token);
          String userInfoStr = HttpUtils.doGet(osrtAppSite + "/api/users", null,
              header);
          getLog().info("****" + userInfoStr);
          if (ObjectUtils.isEmpty(userInfoStr)) {
            getLog().error(
                "Token expired or invalid!please try to login again!");
            continue;
          } else {
            userMap = om.readValue(userInfoStr, Map.class);
          }
          return true;
        } else {
          continue;
        }
      } else if ("n".equalsIgnoreCase(yn) || "no".equalsIgnoreCase(yn)) {
        getLog().info("Skipping install osrt app");
        return false;
      } else {
        continue;
      }
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

  private Scope initScope(Scanner sc, String osrtAppSite, Header header)
      throws JsonProcessingException {
    Scope scope = new Scope();
    while (true) {
      getLog().info("Which scope do you want to deploy to? \n 1 "
          + "personal account \n 2 organization" + " ");
      int type = sc.nextInt();
      if (type == 1) {
        scope.setType("user");
        scope.setId(userMap.get("id").toString());
        break;
      } else if (type == 2) {
        //查询返回当前的user所在的有上传权限的org
        String countStr = HttpUtils.doGet(
            osrtAppSite + "/api/organizations/count", null, header);
        if (ObjectUtils.isEmpty(countStr) || Integer.valueOf(countStr) == 0) {
          getLog().warn("Your have not join any organization!");
          continue;
        } else {
          while (true) {
            getLog().info("please input the organization name: ");
            String orgName = sc.nextLine();
            Map<String,Object> nameMap = new HashMap<>();
            nameMap.put("name", orgName);
            String memberRoleStr = HttpUtils.doGet(
                osrtAppSite + "/api/organizations/role", nameMap, header);
            if (ObjectUtils.isEmpty(memberRoleStr)) {
              getLog().warn(
                  "You are not the organization member, or you don’t have "
                      + "access to the deploy anymore ");
              continue;
            } else {
              //todo 简单设计,只有org中的owener可以deploy;应该校验当前的user是否有deploy access
              Map map = om.readValue(memberRoleStr, Map.class);
              Integer orgId = Integer.valueOf(map.get("orgId").toString());
              String accessRole = map.get("accessRole").toString();
              if (!StringUtils.equalsIgnoreCase(accessRole, "owner")) {
                getLog().warn("You don’t have the access to deploy");
                continue;
              } else {
                scope.setType("organization");
                scope.setId(orgId.toString());
                break;
              }
            }
          }
          break;
        }
      } else {
        continue;
      }
    }
    return scope;
  }

  private String initProject(Scanner sc, String osrtAppSite, Header header)
      throws JsonProcessingException {
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
        nameMap.put("scopeId", Integer.valueOf(config.getScope().getId()));
        nameMap.put("type", config.getScope().getType());
        String prjStr = HttpUtils.doGet(osrtAppSite + "/api/projects/check",
            nameMap, header);
        if (StringUtils.isNotBlank(prjStr)) {
          Map<String,Object> prjMap = om.readValue(prjStr, Map.class);
          projectMap.put("projectId", prjMap.get("id"));
          projectMap.put("name", prjMap.get("name"));

          Map<String,Object> map = Maps.newHashMap();
          map.put("scopeId", Integer.valueOf(config.getScope().getId()));
          map.put("type", config.getScope().getType());
          map.put("projectId", prjMap.get("id"));
          map.put("bundleStr", getBundleStr(app.getBundle()));
          HttpUtils.doPost(osrtAppSite + "/api/projects/app-bundle", map,
              header);
          break;
        } else {
          getLog().warn("Project not exists,or you have no access!");
          continue;
        }
        //新增
      } else if ("n".equalsIgnoreCase(yn)) {
        String projectName;
        while (true) {
          getLog().info("What’s your project’s name?");
          getLog().info("(project name can only consist of up to 100 "
              + "aiphanumeric lowercase characters.Hyphens can be used in "
              + "between the name,but never at the start or end.)");
          projectName = sc.nextLine().toLowerCase();
          if (!projectName.matches("^(?!-)(?!.*?-$)[a-zA-Z0-9-]{1,100}$")) {
            getLog().warn("wrong input format!");
            continue;
          }
          break;
        }
        nameMap.put("name", projectName);
        nameMap.put("bundleStr", getBundleStr(app.getBundle()));
        nameMap.put("scopeId", Integer.valueOf(config.getScope().getId()));
        nameMap.put("type", config.getScope().getType());
        String prjStr = HttpUtils.doPost(osrtAppSite + "/api/projects/app-add",
            nameMap, header);
        if (StringUtils.isNotBlank(prjStr)) {
          Map<String,Object> prjMap = om.readValue(prjStr, Map.class);
          projectMap.put("projectId", prjMap.get("id"));
          projectMap.put("name", prjMap.get("name"));
          break;
        } else {
          getLog().info("Project already exists! Or you have to cancel the "
              + "bundle relations ");
          continue;
        }
      } else {
        getLog().info("Wrong input value.");
        continue;
      }
    }
    return projectId;
  }

  private String getBundleStr(List<String> bundle) {
    if (!ObjectUtils.isEmpty(bundle)) {
      String[] toArray = bundle.toArray(new String[bundle.size()]);
      String bundleStr = String.join("/", toArray);
      return bundleStr;
    }
    return null;
  }

  public static void main(String[] args) {
    String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NTIyMDk4NTcsInVzZXJfbmFtZSI6ImNwZiIsImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiSmdCcjhCcGJlWkNaVVg3cjVmdDg1V1A3X2lFIiwiY2xpZW50X2lkIjoibWFwbGVjbG91ZHkiLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.OC9SUwDeMB2Pkk5falmF4riBjHtbu-muvO8iB5qxJgZU70CePGVgLAn6R5PCwZm-F5XyXDbYQ2csJcrGzZXQ_E6KeHPeT3OmWnnLNXBQDxR6KnOsy-mt6gRVh1JvChkK4gp_s08ybMFUzSnzXbCfI40oSmbg6F34RQSQ4qLROgLxCqzocrqQHtTjW2nS2AhmiMZkRxfDzxaB-DTMkUKFMixjkaiclJDR5oMX9yjPoQThBQr-ti3c87dOHB8xpdoncvTX6syFd3rLBqx9nIs7Z8dOfJb7F4_D6pXCfd8TVDNyPBYsw1tZfvPzvmCRHeuz2LunNiM6FRniR2bl4jdyIw";
    Header header = new Header();
    header.setName("Authorization");
    header.setValue("Bearer " + token);

    HashMap<String,Object> nameMap = new HashMap<>();
    nameMap.put("name", "fdsaf");
    nameMap.put("bundleStr", "com.macro.mall/mall-admin");
    nameMap.put("scopeId", "26");
    nameMap.put("type", "user");
    String prjStr = HttpUtils.doPost(
        "http://localhost:16891" + "/api/projects" + "/app" + "-add", nameMap,
        header);

    System.out.println(prjStr);
  }

}
