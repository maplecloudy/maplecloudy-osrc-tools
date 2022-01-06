package com.maplecloudy.osrt.boot.maven;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.maplecloudy.osrt.model.app.Config;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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
import org.springframework.util.ObjectUtils;

import java.io.File;
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

  private static final List<ProjectInstallerRequest> INSTALLREQUESTS = Collections
      .synchronizedList(new ArrayList<ProjectInstallerRequest>());

  /**
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

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().info("Skipping install osrt app");
    } else {
      installProject();
    }
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
      ObjectMapper om = new ObjectMapper();
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
          if (!ObjectUtils.isEmpty(config) && !ObjectUtils
              .isEmpty(config.getRemote())) {
            osrtAppSite = config.getRemote();
          } else {
            config = new Config();
            config.setRemote("https://www.osrc.com");
            osrtAppSite = config.getRemote();
          }
        }
        if (StringUtils.isBlank(osrtAppToken)) {
          if (!ObjectUtils.isEmpty(config) && !ObjectUtils
              .isEmpty(config.getAccessToken())) {
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
          mg = new GetMethod(osrtAppSite + "/api/user");
          mg.addRequestHeader(header);
          int mgRespStatus = hc.executeMethod(mg);
          if (mgRespStatus == 200) {
            Map map = om.readValue(mg.getResponseBody(), Map.class);
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
                "failed with code: " + mgRespStatus + ",error message:" + mg
                    .getResponseBodyAsString());
            boolean flag = checkLoginInfo(osrtAppSite, header, hc, target);
            if (!flag) {
              return;
            }
          }
        }

        targerJar = new JarFile(target);

        JarEntry indexEntry = targerJar.getJarEntry("index.yml");
        if (indexEntry == null) {
          getLog().error("install osrt skip package not have [index.yml]!");
          return;
        }
        byte[] index = sun.misc.IOUtils
            .readFully(targerJar.getInputStream(indexEntry),
                (int) indexEntry.getSize(), true);
        ByteArrayPartSource bps = new ByteArrayPartSource("index.yml", index);
        FilePart indexFilePart = new FilePart("index", bps,
            FilePart.DEFAULT_CONTENT_TYPE, StandardCharsets.UTF_8.name());
        getLog().info("start to verify app deploy enabled or not");
        PostMethod check = new PostMethod(osrtAppSite + "/api/apps/check");
        check.addRequestHeader(header);
        Part[] indexPart = {indexFilePart};
        MultipartRequestEntity mreIndex = new MultipartRequestEntity(indexPart,
            check.getParams());
        // FileRequestEntity fileRequestEntity = new
        // FileRequestEntity(target,"multipart/form-data");
        check.setRequestEntity(mreIndex);
        int code = hc.executeMethod(check);
        if (code == 200) {
          Map map = om.readValue(check.getResponseBody(), Map.class);
          Integer innerCode = (Integer) map.get("code");
          if (200 == innerCode) {
            getLog().info(map.get("msg").toString());
          } else {
            getLog().error(map.get("msg") + "\nApp info: " + om
                .writeValueAsString(map.get("data")));
            return;
          }
        } else {
          getLog().error(
              "install osrt file:" + target + " failed with code: " + code
                  + ",error message:" + check.getResponseBodyAsString());
          return;
        }

        m = new PostMethod(osrtAppSite + "/api/apps/install-app-file");
        m.addRequestHeader(header);
        Part[] parts = {indexFilePart, new FilePart("appFile", target)};

        MultipartRequestEntity mre = new MultipartRequestEntity(parts,
            m.getParams());
        // FileRequestEntity fileRequestEntity = new
        // FileRequestEntity(target,"multipart/form-data");
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
    ObjectMapper om = new ObjectMapper();
    PostMethod m = null;
    while (true) {
      getLog().info("please login (y/n)? ");
      String yn = sc.nextLine();
      if ("y".equalsIgnoreCase(yn) || "yes".equalsIgnoreCase(yn)) {
        getLog().info("please input the username: ");
        String username = sc.nextLine();
        getLog().info("please input the password: ");
        String password = sc.nextLine();
        m = new PostMethod(osrtAppSite + "/api/accounts/signin");
        Map<String,String> map = Maps.newHashMap();
        map.put("username", username);
        map.put("password", password);
        String userInfoStr = om.writeValueAsString(map);
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

  public static void main(String[] args) {

  }

}
