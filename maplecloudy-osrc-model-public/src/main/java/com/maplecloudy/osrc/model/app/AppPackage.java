package com.maplecloudy.osrc.model.app;

import io.swagger.v3.oas.annotations.media.Schema;

public class AppPackage {

  @Schema(description = "应用包的名称")
  private String packageName;
  @Schema(description = "应用包的类型，FILE将以文件的形式呈现在Container的work Dir，ARCHIVE将自动被解压缩后呈现在Container的work Dir")
  private AppPackageType type;

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public AppPackageType getType() {
    return type;
  }

  public void setType(AppPackageType type) {
    this.type = type;
  }
}
