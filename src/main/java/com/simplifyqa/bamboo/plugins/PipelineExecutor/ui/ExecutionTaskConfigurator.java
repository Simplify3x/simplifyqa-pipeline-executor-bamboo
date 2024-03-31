package com.simplifyqa.bamboo.plugins.PipelineExecutor.ui;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionService;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecutionTaskConfigurator
  extends AbstractTaskConfigurator
  implements DeploymentPipeline {

  protected static final String exec_token = "TOKEN";
  protected static final String app_url = "APP_URL";
  protected static final double threshold = "THRESHOLD";
  protected static final boolean verbose = "VERBOSE";

  @Override
  public void populateContextForCreate(
    @NotNull final Map<String, Object> context
  ) {
    super.populateContextForCreate(context);
  }

  @Override
  public void populateContextForEdit(
    @NotNull final Map<String, Object> context,
    @NotNull final TaskDefinition taskDefinition
  ) {
    super.populateContextForEdit(context, taskDefinition);
    taskConfiguratorHelper.populateContextWithConfiguration(
      context,
      taskDefinition,
      getFieldsToCopy()
    );
  }

  @Override
  public void populateContextForView(
    @NotNull final Map<String, Object> context,
    @NotNull final TaskDefinition taskDefinition
  ) {
    super.populateContextForView(context, taskDefinition);
    taskConfiguratorHelper.populateContextWithConfiguration(
      context,
      taskDefinition,
      getFieldsToCopy()
    );
  }

  @NotNull
  @Override
  public Map<String, String> generateTaskConfigMap(
    @NotNull final ActionParametersMap params,
    @Nullable final TaskDefinition previousTaskDefinition
  ) {
    final Map<String, String> config = super.generateTaskConfigMap(
      params,
      previousTaskDefinition
    );
    taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(
      config,
      params,
      getFieldsToCopy()
    );
    return config;
  }

  protected List<String> getFieldsToCopy() {
    return ImmutableList
      .<String>builder()
      .add(TOKEN, APP_URL, THRESHOLD, VERBOSE)
      .addAll(getRequiredFiles())
      .build();
  }

  // Constructor
  public ExecutionTaskConfigurator(
    String exec_token,
    String app_url,
    double threshold,
    boolean verbose
  ) {
    if ((threshold < 1.00) || (threshold > 100.00)) threshold = 100.00;

    if (
      !(
        app_url.startsWith("http://") ^
        app_url.startsWith("https://") ^
        app_url.startsWith("localhost:")
      )
    ) app_url = "https://simplifyqa.app";

    this.exec_token = exec_token;
    this.app_url = app_url;
    this.threshold = threshold;
    this.verbose = verbose;
  }

  // Getters
  public String getExec_token() {
    return this.exec_token;
  }

  public String getApp_url() {
    return this.app_url;
  }

  public double getThreshold() {
    return this.threshold;
  }

  public boolean getVerbose() {
    return this.verbose;
  }

  // Setters
  protected void setExec_token(String exec_token) {
    this.exec_token = exec_token;
  }

  protected void setApp_url(String app_url) {
    if (
      !(
        app_url.startsWith("http://") ^
        app_url.startsWith("https://") ^
        app_url.startsWith("localhost:")
      )
    ) app_url = "https://simplifyqa.app";

    this.app_url = app_url;
  }

  protected void setThreshold(double threshold) {
    if ((threshold < 1.00) || (threshold > 100.00)) threshold = 100.00;

    this.threshold = threshold;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }
}
