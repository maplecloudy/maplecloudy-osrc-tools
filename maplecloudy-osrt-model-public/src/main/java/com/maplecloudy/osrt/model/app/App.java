package com.maplecloudy.osrt.model.app;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class App extends AbstractApp{
  @Schema(description = "此应用依赖的tools应用")
  public List<Tools> tools;
  public List<PodEntry> podEntries;
}
