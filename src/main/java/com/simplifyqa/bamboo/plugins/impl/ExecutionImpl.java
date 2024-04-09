package com.simplifyqa.bamboo.plugins.impl;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.simplifyqa.bamboo.plugins.api.Execution;
import java.io.IOException;

public class ExecutionImpl extends Execution {

  public ExecutionImpl(
    String exec_token,
    String app_url,
    double threshold,
    boolean verbose,
    BuildLogger logger
  ) throws IOException {
    super(exec_token, app_url, threshold, verbose, logger);
  }
}
