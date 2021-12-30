package com.maplecloudy.osrt.model.repository;

import io.swagger.v3.oas.annotations.media.Schema;

public class Repository {
  @Schema(description = "代码仓库的SCM地址")
  public String url;
  @Schema(description = "来源网站")
  public String type;

}
