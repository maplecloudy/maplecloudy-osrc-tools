package com.maplecloudy.osrt.model.app;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class App extends AbstractApp{
  @Schema(description = "此应用依赖的tools应用")
  private List<Tools> tools;
  private List<PodEntry> podEntries;

  public List<Tools> getTools() {
    return tools;
  }

  public void setTools(List<Tools> tools) {
    this.tools = tools;
  }

  public List<PodEntry> getPodEntries() {
    return podEntries;
  }

  public void setPodEntries(List<PodEntry> podEntries) {
    this.podEntries = podEntries;
  }
}
