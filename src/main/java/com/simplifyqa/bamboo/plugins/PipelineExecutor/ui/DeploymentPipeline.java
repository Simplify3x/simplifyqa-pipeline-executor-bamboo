package com.simplifyqa.bamboo.plugins.PipelineExecutor.ui;

import java.util.List;

public interface DeploymentPipeline {
  String getPipelineName();
  List<String> getRequiredFiles();
}
