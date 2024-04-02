package com.simplifyqa.bamboo.plugins.PipelineExecutor.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.CommonTaskContext;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Execute implements TaskType {

  @NotNull
  @Override
  public TaskResult execute(@NotNull final TaskContext taskContext)
    throws TaskException {
    final BuildLogger buildLogger = taskContext.getBuildLogger();

    final String token = taskContext.getConfigurationMap().get("EXEC_TOKEN");
    final String app_url = taskContext.getConfigurationMap().get("APP_URL");
    final String threshold = taskContext.getConfigurationMap().get("THRESHOLD");
    final String verbose = taskContext.getConfigurationMap().get("VERBOSE");

    buildLogger.addBuildLogEntry(token);
    buildLogger.addBuildLogEntry(app_url);
    buildLogger.addBuildLogEntry(threshold);
    buildLogger.addBuildLogEntry(verbose);

    // I18nBean textProvider = getI18nBean();
    // buildLogger.addBuildLogEntry(
    //   getI18nBean().getText("com.simplifyqa.fields.token.nameKey")
    // );
    // buildLogger.addBuildLogEntry(
    //   getI18nBean().getText("com.simplifyqa.fields.url.nameKey")
    // );
    // buildLogger.addBuildLogEntry(
    //   getI18nBean().getText("com.simplifyqa.fields.threshold.nameKey")
    // );
    // buildLogger.addBuildLogEntry(
    //   getI18nBean().getText("com.simplifyqa.fields.verbose.nameKey")
    // );

    return TaskResultBuilder.newBuilder(taskContext).success().build();
  }
}
