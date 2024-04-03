package com.simplifyqa.bamboo.plugins.PipelineExecutor.ui;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.simplifyqa.bamboo.plugins.PipelineExecutor.ExecutionConstants;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecutionTaskConfigurator extends AbstractTaskConfigurator {

  private I18nResolver i18nResolver;
  private final Logger.Log log = Logger.getInstance(this.getClass());

  private final String EXEC_TOKEN =
    this.getLabel(ExecutionConstants.EXEC_TOKEN_FIELD);
  private final String APP_URL =
    this.getLabel(ExecutionConstants.APP_URL_FIELD);
  private final String THRESHOLD =
    this.getLabel(ExecutionConstants.THRESHOLD_FIELD);
  private final String VERBOSE =
    this.getLabel(ExecutionConstants.VERBOSE_FIELD);

  public void CreateDeploymentConfigurator(
    @ComponentImport I18nResolver i18nResolver
  ) {
    this.i18nResolver = i18nResolver;
  }

  @Override
  @NotNull
  public Map<String, String> generateTaskConfigMap(
    @NotNull final ActionParametersMap params,
    @Nullable final TaskDefinition previousTaskDefinition
  ) {
    final Map<String, String> config = super.generateTaskConfigMap(
      params,
      previousTaskDefinition
    );
    config.put(this.EXEC_TOKEN, params.getString(this.EXEC_TOKEN));
    config.put(this.APP_URL, params.getString(this.APP_URL));
    config.put(this.THRESHOLD, params.getString(this.THRESHOLD));
    config.put(this.VERBOSE, params.getString(this.VERBOSE));

    return config;
  }

  @Override
  public void populateContextForCreate(
    @NotNull final Map<String, Object> context
  ) {
    super.populateContextForCreate(context);

    context.put(this.EXEC_TOKEN, "");
    context.put(this.APP_URL, "https://simplifyqa.app");
    context.put(this.THRESHOLD, 100.00);
    context.put(this.VERBOSE, true);
  }

  @Override
  public void populateContextForEdit(
    @NotNull final Map<String, Object> context,
    @NotNull final TaskDefinition taskDefinition
  ) {
    super.populateContextForEdit(context, taskDefinition);

    context.put(
      this.EXEC_TOKEN,
      taskDefinition.getConfiguration().get(this.EXEC_TOKEN)
    );
    context.put(
      this.APP_URL,
      taskDefinition.getConfiguration().get(this.APP_URL)
    );
    context.put(
      this.THRESHOLD,
      taskDefinition.getConfiguration().get(this.THRESHOLD)
    );
    context.put(
      this.VERBOSE,
      taskDefinition.getConfiguration().get(this.VERBOSE)
    );
  }

  @Override
  public void validate(
    @NotNull final ActionParametersMap params,
    @NotNull final ErrorCollection errorCollection
  ) {
    super.validate(params, errorCollection);

    // this.validateToken(params, errorCollection);
    // this.validateAppUrl(params, errorCollection);
    // this.validateThreshold(params, errorCollection);
    // this.validateVerbose(params, errorCollection);
  }

  private boolean validateToken(
    final ActionParametersMap params,
    final ErrorCollection errorCollection
  ) {
    return true;
  }

  private boolean validateAppUrl(
    final ActionParametersMap params,
    final ErrorCollection errorCollection
  ) {
    try {
      String toValidate = params.getString(this.APP_URL);

      if (StringUtils.isBlank(toValidate)) return false; else if (
        !(
          toValidate.startsWith("http://") ^
          toValidate.startsWith("https://") ^
          toValidate.startsWith("localhost:")
        )
      ) {
        if (true) return true; // TODO: validate Connection by sending GET Request
        else {
          return false;
        }
      } else {
        return false;
      }
    } catch (RuntimeException e) {
      log.error(
        String.format(
          "Unexpected results trying to validate ApiKey: Reason '%s'",
          e.getMessage()
        )
      );

      // errorCollection.addError(
      //   APP_URL,

      // );
      return false;
    }
  }

  private boolean validateThreshold(
    final ActionParametersMap params,
    final ErrorCollection errorCollection
  ) {
    return true;
  }

  private boolean validateVerbose(
    final ActionParametersMap params,
    final ErrorCollection errorCollection
  ) {
    return true;
  }

  private String getLabel(String key) {
    return i18nResolver.getText(key);
  }
}
