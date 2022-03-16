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
  private Integer id;
  private String entry;
  @Schema(description = "service/task")
  private AppPodType type;

  private List<String> bundle;

  private String name;

  private String avatarUrl;

  @Schema(description = "owner运行的runtime的数量")
  private Integer ownerRunsCount;
  @Schema(description = "当前user的runtime的数量")
  private Integer userRunsCount;

  private boolean original;

  private String domainUrl;
  private String md5DomainUrl;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getEntry() {
    return entry;
  }

  public void setEntry(String entry) {
    this.entry = entry;
  }

  public AppPodType getType() {
    return type;
  }

  public void setType(AppPodType type) {
    this.type = type;
  }

  public List<String> getBundle() {
    return bundle;
  }

  public void setBundle(List<String> bundle) {
    this.bundle = bundle;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public Integer getOwnerRunsCount() {
    return ownerRunsCount;
  }

  public void setOwnerRunsCount(Integer ownerRunsCount) {
    this.ownerRunsCount = ownerRunsCount;
  }

  public Integer getUserRunsCount() {
    return userRunsCount;
  }

  public void setUserRunsCount(Integer userRunsCount) {
    this.userRunsCount = userRunsCount;
  }

  public boolean isOriginal() {
    return original;
  }

  public void setOriginal(boolean original) {
    this.original = original;
  }

  public String getDomainUrl() {
    return domainUrl;
  }

  public void setDomainUrl(String domainUrl) {
    this.domainUrl = domainUrl;
  }

  public String getMd5DomainUrl() {
    return md5DomainUrl;
  }

  public void setMd5DomainUrl(String md5DomainUrl) {
    this.md5DomainUrl = md5DomainUrl;
  }
}
