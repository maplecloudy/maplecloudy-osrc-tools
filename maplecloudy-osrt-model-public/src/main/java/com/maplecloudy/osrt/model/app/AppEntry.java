package com.maplecloudy.osrt.model.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * @author cpf
 * @create 2021-11-17
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppEntry {
  @Schema(description = "app pod的id")
  public Integer id;
  public String entry;
  @Schema(description = "service/task")
  public AppPodType type;

  public List<String> bundle;

  public String name;

  public String avatarUrl;

  @Schema(description = "owner运行的runtime的数量")
  public Integer ownerRunsCount;
  @Schema(description = "当前user的runtime的数量")
  public Integer userRunsCount;

  public boolean original;

  public String domainUrl;
  public String md5DomainUrl;

}
