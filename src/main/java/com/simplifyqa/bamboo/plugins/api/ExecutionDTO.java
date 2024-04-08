package com.simplifyqa.bamboo.plugins.api;

import com.simplifyqa.bamboo.plugins.impl.ExecutionDTOImpl;

// Execution Object
public abstract class ExecutionDTO {

  private ExecutionDTOImpl exec_obj;

  public void ExecutionDTO() {}

  public void ExecutionDTO(ExecutionDTOImpl exec_obj) {
    this.exec_obj = exec_obj;
  }
}
