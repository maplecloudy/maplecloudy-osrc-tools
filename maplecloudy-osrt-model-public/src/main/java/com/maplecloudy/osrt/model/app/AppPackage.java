package com.maplecloudy.osrt.model.app;

import io.swagger.v3.oas.annotations.media.Schema;

public class AppPackage {
  public static enum Type{
    FILE,ARCHIVE
  }
  @Schema(description = "应用包的名称")
  public String packageName;
  @Schema(description = "应用包的类型，FILE将以文件的形式呈现在Container的work Dir，ARCHIVE将自动被解压缩后呈现在Container的work Dir")
  public Type type;
  
}
