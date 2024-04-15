package com.simplifyqa.bamboo.plugins;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskState;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.simplifyqa.bamboo.plugins.api.ExecutionServices;
import com.simplifyqa.bamboo.plugins.api.ExecutionState;
import com.simplifyqa.bamboo.plugins.impl.ExecutionImpl;
import com.simplifyqa.bamboo.plugins.impl.ExecutionServicesImpl;
import java.io.IOException;
import org.json.simple.JSONObject;

public class ExecutionTask implements TaskType {

  private I18nResolver textProvider;
  private BuildLogger buildLogger;

  public void ExecutionTask(@ComponentImport I18nResolver i18nResolver) {
    this.textProvider = i18nResolver;
  }

  @Override
  public TaskResult execute(final TaskContext taskContext)
    throws TaskException {
    this.buildLogger = taskContext.getBuildLogger();

    return TaskResultBuilder
      .newBuilder(taskContext)
      .setState(this.performExecution(taskContext))
      .build();
  }

  public TaskState performExecution(TaskContext taskContext) {
    ExecutionImpl exec_obj = null;
    try {
      exec_obj =
        new ExecutionImpl(
          taskContext
            .getConfigurationMap()
            .get(ExecutionConstants.EXEC_TOKEN_FIELD),
          taskContext
            .getConfigurationMap()
            .get(ExecutionConstants.APP_URL_FIELD),
          Double.valueOf(
            taskContext
              .getConfigurationMap()
              .get(ExecutionConstants.THRESHOLD_FIELD)
          ),
          Boolean.valueOf(
            taskContext
              .getConfigurationMap()
              .get(ExecutionConstants.VERBOSE_FIELD)
          ),
          taskContext.getBuildLogger()
        );
    } catch (NumberFormatException e) {
      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() + "EXECUTION FAILED!!"
        );
      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          "REASON OF FAILURE: Could not convert the threshold value: " +
          taskContext
            .getConfigurationMap()
            .get(ExecutionConstants.THRESHOLD_FIELD)
        );

      String asterisks = "";
      for (int i = 0; i < 51; i++) asterisks += "*";

      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          asterisks +
          "EOF" +
          asterisks +
          "\n"
        );
      e.printStackTrace();

      return TaskState.FAILED;
    } catch (IOException e) {
      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() + "EXECUTION FAILED!!"
        );

      String asterisks = "";
      for (int i = 0; i < 51; i++) asterisks += "*";

      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          asterisks +
          "EOF" +
          asterisks +
          "\n"
        );
      e.printStackTrace();

      return TaskState.FAILED;
    }

    ExecutionServicesImpl exec_dto = new ExecutionServicesImpl(exec_obj);
    ExecutionState exec_state = exec_dto.startExec();

    if (exec_state == ExecutionState.FAILED) {
      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() + "EXECUTION FAILED!!"
        );

      String asterisks = "";
      for (int i = 0; i < 51; i++) asterisks += "*";

      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          asterisks +
          "EOF" +
          asterisks +
          "\n"
        );

      return TaskState.FAILED;
    } else {
      int executed = exec_obj.getExecutedTcs();

      while (
        (exec_dto.checkExecStatus() == ExecutionState.INPROGRESS) &&
        (exec_obj.getThreshold() > exec_obj.getFailPercent())
      ) {
        if (executed < exec_obj.getExecutedTcs()) {
          exec_dto.printStats();
          executed++;

          if (exec_obj.getThreshold() <= exec_obj.getFailPercent()) {
            taskContext
              .getBuildLogger()
              .addBuildLogEntry(
                "\n" +
                ExecutionServicesImpl.getTimestamp() +
                "THRESHOLD REACHED!!!"
              );
            exec_obj.setExecStatus(ExecutionState.FAILED);
            break;
          }
        }
      }

      exec_dto.checkExecStatus();
      exec_dto.printStats();

      if (exec_obj.getThreshold() <= exec_obj.getFailPercent()) {
        exec_obj.setExecStatus(ExecutionState.FAILED);
        taskContext
          .getBuildLogger()
          .addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() + "EXECUTION FAILED!!"
          );

        if (exec_dto.killExec()) taskContext
          .getBuildLogger()
          .addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: SUCCESSFUL to explicitly kill the execution!\n"
          ); else taskContext
          .getBuildLogger()
          .addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: FAILED to explicitly kill the execution!\n"
          );
      } else {
        exec_obj.setExecStatus(ExecutionState.COMPLETED);
        taskContext
          .getBuildLogger()
          .addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() + "EXECUTION PASSED!!"
          );
      }

      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          "REPORT URL: " +
          exec_obj.getReportUrl() +
          "\n"
        );
    }

    if (exec_obj.getVerbose()) {
      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServices.getTimestamp() +
          "API CALLED: " +
          exec_obj.getCalledAPI()
        );
      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          "REQUEST BODY: " +
          exec_obj.getReqBody()
        );
      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          "RESPONSE BODY: " +
          exec_obj.getRespBody()
        );
    }
    String asterisks = "";
    for (int i = 0; i < 51; i++) asterisks += "*";
    taskContext
      .getBuildLogger()
      .addBuildLogEntry(
        ExecutionServicesImpl.getTimestamp() +
        asterisks +
        "EOF" +
        asterisks +
        "\n"
      );

    if (
      exec_obj.getExecStatus() == ExecutionState.FAILED
    ) return TaskState.FAILED; else return TaskState.SUCCESS;
  }
}
