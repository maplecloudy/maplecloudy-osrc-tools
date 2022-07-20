package com.maplecloudy.osrc.model.app;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public class Tools extends AbstractApp {
  @Schema(description = "tools应用提供对应的shell扩展,应用本身的环境变量配置，通过环境变量加载对应的命令")
  private Map<String,String> env;

  @Schema(description = "工具以单一package方式呈现，在使用前需要预制的命令进行安装，安装以绿色方式进行，仅可以安装到当前container的workdir")
  private List<String> preCmds;

  public Tools() {
    this.setType(AppType.TOOLS);
  }


  public Map<String,String> getEnv() {
    return env;
  }

  public void setEnv(Map<String,String> env) {
    this.env = env;
  }

  public List<String> getPreCmds() {
    return preCmds;
  }

  public void setPreCmds(List<String> preCmds) {
    this.preCmds = preCmds;
  }
}
