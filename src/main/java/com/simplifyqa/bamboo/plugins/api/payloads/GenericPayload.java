package com.simplifyqa.bamboo.plugins.api.payloads;

import com.simplifyqa.bamboo.plugins.api.Execution;

public class GenericPayload {

  private String payload;

  // Constructor
  public GenericPayload(String payload) {
    this.payload = payload;
  }

  // Getter
  public String getPayload() {
    return this.payload;
  }

  // Setter
  public void setPayload(String payload) {
    this.payload = payload;
  }

  // Factory methods to create different payloads using DAO object
  public static GenericPayload createTriggerPayload(Execution execution) {
    return new GenericPayload(
      "{\"token\":\"" + execution.getExec_token() + "\"}"
    );
  }

  public static GenericPayload createStatusPayload(Execution execution) {
    return new GenericPayload(
      "{\"executionId\":" +
      execution.getExecId() +
      ",\"customerId\":" +
      execution.getCustomerId() +
      ",\"projectId\":" +
      execution.getProjectId() +
      "}"
    );
  }

  public static GenericPayload createKillPayload(Execution execution) {
    return new GenericPayload(
      "{\"customerId\":" +
      execution.getCustomerId() +
      ",\"id\":" +
      execution.getExecId() +
      ",\"userId\":" +
      execution.getUserId() +
      "}"
    );
  }
}
