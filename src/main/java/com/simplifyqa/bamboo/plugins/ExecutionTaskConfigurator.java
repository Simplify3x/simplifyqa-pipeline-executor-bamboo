package com.simplifyqa.bamboo.plugins;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.simplifyqa.bamboo.plugins.impl.ExecutionServicesImpl;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecutionTaskConfigurator extends AbstractTaskConfigurator {

  private static Properties textProvider;
  private static I18nResolver i18nResolver;

  public void ExecutionTaskConfigurator(
    @ComponentImport I18nResolver i18nResolver
  ) {
    // ExecutionTaskConfigurator.textProvider =
    //   ExecutionTaskConfigurator.readPropertiesFile(
    //     ExecutionConstants.PROPERTIES_PATH
    //   );

    ExecutionTaskConfigurator.i18nResolver = i18nResolver;
    // ExecutionTaskConfigurator.textProvider.forEach((key, value) ->
    // System.out.println(key + " : " + value));
  }

  @Override
  public Map<String, String> generateTaskConfigMap(
    @NotNull final ActionParametersMap params,
    @Nullable final TaskDefinition previousTaskDefinition
  ) {
    final Map<String, String> config = super.generateTaskConfigMap(
      params,
      previousTaskDefinition
    );

    config.put(
      ExecutionConstants.EXEC_TOKEN_FIELD,
      params.getString(ExecutionConstants.EXEC_TOKEN_FIELD)
    );

    config.put(
      ExecutionConstants.APP_URL_FIELD,
      params.getString(ExecutionConstants.APP_URL_FIELD)
    );

    config.put(
      ExecutionConstants.THRESHOLD_FIELD,
      params.getString(ExecutionConstants.THRESHOLD_FIELD)
    );

    config.put(
      ExecutionConstants.VERBOSE_FIELD,
      params.getString(ExecutionConstants.VERBOSE_FIELD)
    );

    return config;
  }

  @Override
  public void validate(
    @NotNull final ActionParametersMap params,
    @NotNull final ErrorCollection errorCollection
  ) {
    super.validate(params, errorCollection);

    this.validateExecToken(params, errorCollection);

    this.validateAppUrl(params, errorCollection);

    this.validateThreshold(params, errorCollection);
  }

  private void validateExecToken(
    ActionParametersMap params,
    ErrorCollection errorCollection
  ) {
    String exec_token = params.getString(ExecutionConstants.EXEC_TOKEN_FIELD);

    if (StringUtils.isEmpty(exec_token)) errorCollection.addError(
      ExecutionConstants.EXEC_TOKEN_FIELD,
      ExecutionConstants.ERR_EMPTY_TOKEN
    );

    if (exec_token.length() != 88) errorCollection.addError(
      ExecutionConstants.EXEC_TOKEN_FIELD,
      ExecutionConstants.ERR_INVALID_TOKEN
    );
  }

  private void validateAppUrl(
    ActionParametersMap params,
    ErrorCollection errorCollection
  ) {
    String app_url = params.getString(ExecutionConstants.APP_URL_FIELD);

    if (StringUtils.isEmpty(app_url)) {
      errorCollection.addError(
        ExecutionConstants.APP_URL_FIELD,
        ExecutionConstants.ERR_EMPTY_APP_URL
      );
    }

    if (
      !(
        app_url.startsWith("http://") ^
        app_url.startsWith("https://") ^
        app_url.startsWith("localhost:")
      )
    ) {
      errorCollection.addError(
        ExecutionConstants.APP_URL_FIELD,
        ExecutionConstants.WARN_INVALID_APP_URL
      );
    }

    if (app_url.endsWith("/")) errorCollection.addError(
      ExecutionConstants.APP_URL_FIELD,
      ExecutionConstants.WARN_INVALID_APP_URL
    );

    try {
      if (
        new ExecutionServicesImpl()
          .makeHttpGetRequest(app_url)
          .getResponseCode() !=
        200
      ) {
        errorCollection.addError(
          ExecutionConstants.APP_URL_FIELD,
          ExecutionConstants.WARN_INVALID_APP_URL
        );
      }
    } catch (IOException e) {
      errorCollection.addError(
        ExecutionConstants.APP_URL_FIELD,
        ExecutionConstants.WARN_INVALID_APP_URL
      );
      e.printStackTrace();
    }
  }

  private void validateThreshold(
    ActionParametersMap params,
    ErrorCollection errorCollection
  ) {
    try {
      final double threshold = Double.valueOf(
        params.getString(ExecutionConstants.THRESHOLD_FIELD)
      );

      if ((threshold < 1.00) || (threshold > 100.00)) {
        errorCollection.addError(
          ExecutionConstants.THRESHOLD_FIELD,
          ExecutionConstants.WARN_INVALID_THRESHOLD
        );
      }
    } catch (NumberFormatException e) {
      errorCollection.addError(
        ExecutionConstants.THRESHOLD_FIELD,
        ExecutionConstants.WARN_INVALID_THRESHOLD
      );
    }
  }

  @Override
  public void populateContextForCreate(
    @NotNull final Map<String, Object> context
  ) {
    super.populateContextForCreate(context);

    context.put(
      ExecutionConstants.APP_URL_FIELD,
      ExecutionConstants.SQA_BASE_URL
    );

    context.put(ExecutionConstants.VERBOSE_FIELD, false);

    context.put(ExecutionConstants.ADVANCED_CHECK_FIELD, false);

    context.put(ExecutionConstants.THRESHOLD_FIELD, 100.00);
  }

  @Override
  public void populateContextForEdit(
    @NotNull final Map<String, Object> context,
    @NotNull final TaskDefinition taskDefinition
  ) {
    super.populateContextForEdit(context, taskDefinition);

    context.put(
      ExecutionConstants.APP_URL_FIELD,
      taskDefinition.getConfiguration().get(ExecutionConstants.APP_URL_FIELD)
    );

    context.put(
      ExecutionConstants.VERBOSE_FIELD,
      taskDefinition.getConfiguration().get(ExecutionConstants.VERBOSE_FIELD)
    );

    context.put(
      ExecutionConstants.THRESHOLD_FIELD,
      taskDefinition.getConfiguration().get(ExecutionConstants.THRESHOLD_FIELD)
    );

    context.put(
      ExecutionConstants.ADVANCED_CHECK_FIELD,
      taskDefinition
        .getConfiguration()
        .get(ExecutionConstants.ADVANCED_CHECK_FIELD)
    );
  }

  public static Properties readPropertiesFile(String filePath) {
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(filePath)) {
      properties.load(fis);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return properties;
  }
}
