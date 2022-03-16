package com.maplecloudy.osrt.model.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author cpf
 * @create 2021-11-15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PodEntry{
  private String cmd;
  private AppPodType appPodType;
  private String entry;

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public AppPodType getAppPodType() {
    return appPodType;
  }

  public void setAppPodType(AppPodType appPodType) {
    this.appPodType = appPodType;
  }

  public String getEntry() {
    return entry;
  }

  public void setEntry(String entry) {
    this.entry = entry;
  }
}
