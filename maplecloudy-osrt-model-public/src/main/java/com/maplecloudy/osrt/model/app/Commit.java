package com.maplecloudy.osrt.model.app;

import io.swagger.v3.oas.annotations.media.Schema;

public class Commit {
  @Schema(description = "代码分支")
  public String branch;
  @Schema(description = "代码当前的commit hash")
  public String commitId;
}
