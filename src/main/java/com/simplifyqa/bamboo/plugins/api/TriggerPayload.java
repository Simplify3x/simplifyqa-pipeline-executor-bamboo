package com.simplifyqa.bamboo.plugins.api;

public class TriggerPayload {

  String token;

  // Constructor
  public TriggerPayload(String token) {
    this.token = token;
  }

  // Getters
  public String getPayload() {
    StringBuilder sb = new StringBuilder("");
    sb.append("{\"token\":\"" + this.token + "\"}");

    return sb.toString();
  }

  public String getToken() {
    return this.token;
  }

  // Setters
  protected void setToken(String token) {
    this.token = token;
  }
}
