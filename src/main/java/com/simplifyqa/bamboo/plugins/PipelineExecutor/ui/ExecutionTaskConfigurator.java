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

  protected static final String TOKEN = "TOKEN";
  protected static final String APP_URL = "APP_URL";
  protected static final String THRESHOLD = "THRESHOLD";
  protected static final String VERBOSE = "VERBOSE";

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
}
