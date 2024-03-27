package com.simplifyqa.bamboo.plugins.SQAPipelineExecutor.impl;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecutionTaskConfigurator extends AbstractTaskConfigurator {

  @Override
  public Map<String, String> generateTaskConfigMap(
    @NotNull final ActionParametersMap params,
    @Nullable final TaskDefinition previousTaskDefinition
  ) {
    final Map<String, String> config = super.generateTaskConfigMap(
      params,
      previousTaskDefinition
    );

    config.put("EXEC_TOKEN", params.getString("EXEC_TOKEN"));
    config.put("APP_URL", params.getString("APP_URL"));
    config.put("THRESHOLD", params.getString("THRESHOLD"));
    config.put("VERBOSE", params.getString("VERBOSE"));

    return config;
  }

  public void validate(
    @NotNull final ActionParametersMap params,
    @NotNull final ErrorCollection errorCollection
  ) {
    super.validate(params, errorCollection);
    I18nBean textProvider = getI18nBean();
    final String sayValue = params.getString("say");
    if (StringUtils.isEmpty(sayValue)) {
      errorCollection.addError(
        "say",
        textProvider.getText("helloworld.say.error")
      );
    }
  }

  @Override
  public void populateContextForCreate(
    @NotNull final Map<String, Object> context
  ) {
    super.populateContextForCreate(context);

    context.put(
      "EXEC_TOKEN",
      "U2FsdGVkX19qkmKPNcU7zY6vSFpm6+43gVGpHyup3KvCmPhlvc/asC48At0FLSXWJdeIdryyNbggxBUX2m2zzQ=="
    );
    context.put("APP_URL", "https://qa.simplifyqa.app");
    context.put("THRESHOLD", "100.00");
    context.put("VERBOSE", "true");
  }

  @Override
  public void populateContextForEdit(
    @NotNull final Map<String, Object> context,
    @NotNull final TaskDefinition taskDefinition
  ) {
    super.populateContextForEdit(context, taskDefinition);

    context.put(
      "EXEC_TOKEN",
      taskDefinition.getConfiguration().get("EXEC_TOKEN")
    );
    context.put("APP_URL", taskDefinition.getConfiguration().get("APP_URL"));
    context.put(
      "THRESHOLD",
      taskDefinition.getConfiguration().get("THRESHOLD")
    );
    context.put("VERBOSE", taskDefinition.getConfiguration().get("VERBOSE"));
  }
  //   public Map<String, String> generateTaskConfigMap(
  //     @NotNull final ActionParametersMap params,
  //     @Nullable final TaskDefinition previousTaskDefinition
  //   ) {
  //     final Map<String, String> config = super.generateTaskConfigMap(
  //       params,
  //       previousTaskDefinition
  //     );

  //     config.put("say", params.getString("say"));

  //     return config;
  //   }
}
