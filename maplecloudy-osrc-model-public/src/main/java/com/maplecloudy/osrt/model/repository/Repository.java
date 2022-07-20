package com.maplecloudy.osrt.model.repository;

import io.swagger.v3.oas.annotations.media.Schema;

public class Repository {
  @Schema(description = "代码仓库的SCM地址")
  private String url;
  @Schema(description = "来源网站")
  private String type;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
