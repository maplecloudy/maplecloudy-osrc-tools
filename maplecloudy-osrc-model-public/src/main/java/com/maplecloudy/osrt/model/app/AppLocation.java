package com.maplecloudy.osrt.model.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import com.maplecloudy.osrt.model.repository.Repository;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AppLocation {

  @Schema(description = "应用的类型,SERVICE类型，除非主动停止不会主动停止的应用，比如任何web service，TASK类型，在完成工作后，会自动停止的应用，TOOLS，提供运行环境类的应用，可以被其他应用作为依赖，比如JAVA环境是一个TOOLS应用")
  private AppType type;
  @Schema(description = "决定应用的路径，可以为空，以Java为例，一个应用一般由groupId和artifactId组成")
  private List<String> bundle = Lists.newArrayList();

  @Schema(description = "应用编译时继承版本信息")
  private String version;
  @Schema(description = "应用在发布的时候重新定义的版本信息")
  private String releaseVersion;

  @Schema(description = "对应代码仓库的commit信息，包含branch和Hash")
  private Commit commit;

  @Schema(description = "应用对应的package，一个APP只能包含一个文件，可以是FILE和ARCHIVE两种类型")
  private AppPackage appPackage = new AppPackage();

  private String appPath;

  private String appKey;

  private String osrtAppKey;

  private String osrtAppPath;

  public void setAppPath(String appPath) {
    this.appPath = appPath;
  }

  public void setAppKey(String appKey) {
    this.appKey = appKey;
  }

  @Schema(description = "全路径")
  public String getAppPath() {
    return this.appPath;
  }

  @Schema(description = "根据app的dir生成的key")
  public String getAppKey() {
    return this.appKey;
  }

  @Schema(description = "")
  public String getOsrtAppKey() {
    return this.osrtAppKey;
  }

  @Schema(description = "")
  public String getOsrtAppPath() {
    return this.osrtAppPath;
  }

  public void setOsrtAppKey(String osrtAppKey) {
    this.osrtAppKey = osrtAppKey;
  }

  public void setOsrtAppPath(String osrtAppPath) {
    this.osrtAppPath = osrtAppPath;
  }

  public AppType getType() {
    return type;
  }

  public void setType(AppType type) {
    this.type = type;
  }

  public List<String> getBundle() {
    return bundle;
  }

  public void setBundle(List<String> bundle) {
    this.bundle = bundle;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getReleaseVersion() {
    return releaseVersion;
  }

  public void setReleaseVersion(String releaseVersion) {
    this.releaseVersion = releaseVersion;
  }

  public Commit getCommit() {
    return commit;
  }

  public void setCommit(Commit commit) {
    this.commit = commit;
  }

  public AppPackage getAppPackage() {
    return appPackage;
  }

  public void setAppPackage(AppPackage appPackage) {
    this.appPackage = appPackage;
  }
}
