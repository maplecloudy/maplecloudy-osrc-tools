package com.maplecloudy.osrc.model.app;

import io.swagger.v3.oas.annotations.media.Schema;

public class Commit {
  @Schema(description = "代码分支")
  private String branch;
  @Schema(description = "代码当前的commit hash")
  private String commitId;

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getCommitId() {
    return commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }
}
