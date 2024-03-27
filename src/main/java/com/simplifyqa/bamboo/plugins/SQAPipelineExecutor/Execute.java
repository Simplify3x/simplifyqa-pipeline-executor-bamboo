package com.simplifyqa.bamboo.plugins.SQAPipelineExecutor;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Execute implements TaskType {

  @NotNull
  @java.lang.Override
  public TaskResult execute(@NotNull final TaskContext taskContext)
    throws TaskException {
    final BuildLogger buildLogger = taskContext.getBuildLogger();

    final String say = taskContext.getConfigurationMap().get("say");

    buildLogger.addBuildLogEntry(say);

    return TaskResultBuilder.create(taskContext).success().build();
  }
}
