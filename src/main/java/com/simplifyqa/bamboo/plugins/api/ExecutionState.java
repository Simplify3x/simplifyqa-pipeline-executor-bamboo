package com.simplifyqa.bamboo.plugins.api;

public enum ExecutionState {
  UNINITIALIZED("Uninitialized"),
  INPROGRESS("Inprogress"),
  FAILED("Failed"),
  COMPLETED("Completed");

  private final String state;

  private ExecutionState(String state) {
    this.state = state;
  }

  public String toString() {
    return this.state;
  }

  public static ExecutionState setState(String targetState) {
    for (ExecutionState state : ExecutionState.values()) {
      if (state.state.equalsIgnoreCase(targetState)) {
        return state;
      }
    }
    throw new IllegalArgumentException(
      "Attempt to set INVALID target execution state: " + targetState
    );
  }
}
