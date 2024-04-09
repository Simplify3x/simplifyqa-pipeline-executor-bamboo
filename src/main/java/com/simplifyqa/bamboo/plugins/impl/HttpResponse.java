package com.simplifyqa.bamboo.plugins.impl;

public class HttpResponse {

  private int responseCode;
  private String responseBody;

  public HttpResponse(int responseCode, String responseBody) {
    this.responseCode = responseCode;
    this.responseBody = responseBody;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public String getResponseBody() {
    return responseBody;
  }
}
