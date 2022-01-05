package com.maplecloudy.osrt.model.app;

/**
 * @author cpf
 * @create 2022-01-04
 */
public class Config {
  private String remote;
  private String username;
  private String accessToken;
  private String tokenType;

  public String getRemote() {
    return remote;
  }

  public void setRemote(String remote) {
    this.remote = remote;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }
}
