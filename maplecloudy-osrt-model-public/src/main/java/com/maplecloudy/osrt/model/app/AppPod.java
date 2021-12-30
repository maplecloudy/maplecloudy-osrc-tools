package com.maplecloudy.osrt.model.app;

import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AppPod {
  

  @Schema(description = "App信息")
  public App app;
  public AppPodType type;
  public String cmd;
  public boolean isOriginal = true;
  public Integer appPodId;
  public Hard hard = new Hard();
  public Set<String> extraSource;
  public Map<String,Object> parameter = Maps.newHashMap();
  public Map<String,String> env = new HashMap<>();
  
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
}
