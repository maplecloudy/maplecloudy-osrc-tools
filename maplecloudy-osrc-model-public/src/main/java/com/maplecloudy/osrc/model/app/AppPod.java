package com.maplecloudy.osrc.model.app;

import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AppPod {

  @Schema(description = "App信息")
  private App app;
  private AppPodType type;
  private String cmd;
  private boolean isOriginal = true;
  private Integer appPodId;
  private Hard hard = new Hard();
  private Set<String> extraSource;
  private Map<String,Object> parameter = Maps.newHashMap();
  private Map<String,String> env = new HashMap<>();
  
  public Map<String,String> getEnv() {
    return env;
  }
  
  public void addEnv(String key, String val) {
    env.put(key, val);
  }
  
  public void addExtraSource(String path) {
    if (extraSource == null) {
      extraSource = new HashSet<>();
    }
    extraSource.add(path);
  }

  public App getApp() {
    return app;
  }

  public void setApp(App app) {
    this.app = app;
  }

  public AppPodType getType() {
    return type;
  }

  public void setType(AppPodType type) {
    this.type = type;
  }

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public boolean isOriginal() {
    return isOriginal;
  }

  public void setOriginal(boolean original) {
    isOriginal = original;
  }

  public Integer getAppPodId() {
    return appPodId;
  }

  public void setAppPodId(Integer appPodId) {
    this.appPodId = appPodId;
  }

  public Hard getHard() {
    return hard;
  }

  public void setHard(Hard hard) {
    this.hard = hard;
  }

  public Set<String> getExtraSource() {
    return extraSource;
  }

  public void setExtraSource(Set<String> extraSource) {
    this.extraSource = extraSource;
  }

  public Map<String,Object> getParameter() {
    return parameter;
  }

  public void setParameter(Map<String,Object> parameter) {
    this.parameter = parameter;
  }

  public void setEnv(Map<String,String> env) {
    this.env = env;
  }
}
