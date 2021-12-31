package com.maplecloudy.osrt.boot.maven;

import java.io.File;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

/**
 * Installs the project's main artifact, and any other artifacts attached by
 * other plugins in the lifecycle, to osrt app center.
 * 
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
//        else {
//        
//        if (pomFile != null) {
//          metadata = new ProjectArtifactMetadata(artifact, pomFile);
//          artifact.addMetadata(metadata);
//        }
//      }
      JarFile targerJar = null;
      PostMethod m = null;
      try {
        File target = getTargetFile(this.finalName, this.classifier,
            this.outputDirectory);
        getLog().info("install osrt file:" + target);
        HttpClient hc = new HttpClient();
        hc.getParams().setParameter("http.useragent",
            "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)");
        String osrtAppSite = System.getenv("OSRT_APP_SITE");
        String osrtAppToken = System.getenv("OSRT_APP_TOKEN");
        if (StringUtils.isBlank(osrtAppSite)) {
//          osrtAppSite ="http://192.168.8.103:16881/api/apps/install-app";
          osrtAppSite = "http://www.osrc.com/api/apps/install-app-file";
        }
        m = new PostMethod(osrtAppSite);
//        Header header = new Header("", "multipart/form-data");
//      Header header = new Header("Content-type","multipart/form-data");
//      FilePart filePath = new FilePart(target.getName(),target);
        if (StringUtils.isBlank(osrtAppToken)) {
          osrtAppToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MzI3MjIwNjgsInVzZXJfbmFtZSI6ImNwZiIsImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiREpMYzdoU0Zsa1BTeWFBeXQtalF6RjkxYWVvIiwiY2xpZW50X2lkIjoibWFwbGVjbG91ZHkiLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.lXuz_KyFp76NTqhA9snmS5e0nWcSqhDQGzlG-42dJ6I3BVqbT1x7UF9BdkQNV1pnzMuFcv8nL-UUcLgzWzazswknb5u0f8olddLiP2yXTq4GMUag-I_hmGoPZfmrg8do9JKEYHztwTu-1hjt8SleolfZlCaQyx3AW2KkW9Huq6QMhZc-8cGWLOMfsI_sLWL8KdOgxfYoWjfTr0DFDolKQgwiVLhG6k5o-ufiZXisdssoTXHZu-VLqnEOxEoM2_Qx42pXn9dnPtkiuVi16nlIjGx_vPQW8hnePtJfqyae19hWEwpjPEXI7ILn-tXKN5Wnq97L599JJUGQbAjDAwgD7w";
        }
        getLog().info(osrtAppSite);
        getLog().info(osrtAppToken);

        Header header = new Header("Authorization", "Bearer "+osrtAppToken);
        m.addRequestHeader(header);
        targerJar = new JarFile(target);
        
        JarEntry indexEntry = targerJar.getJarEntry("index.yml");
        if (indexEntry == null) {
          getLog().info("install osrt skip package not have [index.yml]!");
          return;
        }
        byte[] index= sun.misc.IOUtils.readFully(targerJar.getInputStream(indexEntry) ,(int) indexEntry.getSize(), true);
        ByteArrayPartSource bps = new ByteArrayPartSource("index.yml",index);
        Part[] parts = {
            new FilePart("index", bps,FilePart.DEFAULT_CONTENT_TYPE,StandardCharsets.UTF_8.name()),
            new FilePart("appFile", target)};
        
        MultipartRequestEntity mre = new MultipartRequestEntity(parts,
            m.getParams());
//      FileRequestEntity fileRequestEntity = new FileRequestEntity(target,"multipart/form-data");
        m.setRequestEntity(mre);
        
        int respStatus = hc.executeMethod(m);
        if (respStatus == 200) {
          getLog().info("install osrt file:" + target + " sucesss!");
        } else {
          getLog().info("install osrt file:" + target + " failed with code: "
              + respStatus + ",error message:" + m.getResponseBodyAsString());
          
        }
      } finally {
        IOUtils.closeQuietly(targerJar);
        if (m != null) m.releaseConnection();
      }
      
    } catch (IOException e) {
      throw new MojoFailureException("IOException", e);
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
    return new File(targetDirectory, finalName + classifierSuffix + "."
        + this.project.getArtifact().getArtifactHandler().getExtension());
  }
  
  public void setSkip(boolean skip) {
    this.skip = skip;
  }
  
}
