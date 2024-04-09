package com.simplifyqa.bamboo.plugins;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.simplifyqa.bamboo.plugins.api.ExecutionServices;
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

    try {
      if (this.performExecution(taskContext)) return TaskResultBuilder
        .newBuilder(taskContext)
        .success()
        .build(); else return TaskResultBuilder
        .newBuilder(taskContext)
        .failed()
        .build();
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return TaskResultBuilder.newBuilder(taskContext).failed().build();
    } catch (IOException e) {
      e.printStackTrace();
      return TaskResultBuilder.newBuilder(taskContext).failed().build();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return TaskResultBuilder.newBuilder(taskContext).failed().build();
    }
  }

  public void printLog(String toPrint) {
    if (toPrint.trim().length() == 0) return;
    buildLogger.addBuildLogEntry(toPrint);
  }

  public boolean performExecution(TaskContext taskContext)
    throws NumberFormatException, IOException, InterruptedException {
    Boolean retFlag = false;

    ExecutionImpl exec_obj = new ExecutionImpl(
      taskContext
        .getConfigurationMap()
        .get(ExecutionConstants.EXEC_TOKEN_FIELD),
      taskContext.getConfigurationMap().get(ExecutionConstants.APP_URL_FIELD),
      Double.valueOf(
        taskContext
          .getConfigurationMap()
          .get(ExecutionConstants.THRESHOLD_FIELD)
      ),
      Boolean.valueOf(
        taskContext.getConfigurationMap().get(ExecutionConstants.VERBOSE_FIELD)
      ),
      taskContext.getBuildLogger()
    );

    ExecutionServicesImpl exec_dto = new ExecutionServicesImpl(exec_obj);

    if (!exec_dto.startExec()) {
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

      return false;
    } else {
      int executed = exec_obj.getExecutedTcs();

      while (
        (exec_dto.checkExecStatus().equalsIgnoreCase("INPROGRESS")) &&
        (exec_obj.getThreshold() > exec_obj.getFailPercent())
      ) {
        if (executed < exec_obj.getExecutedTcs()) {
          executed++;
          taskContext
            .getBuildLogger()
            .addBuildLogEntry(
              ExecutionServices.getTimestamp() +
              "EXECUTION STATUS: Execution " +
              exec_obj.getExecStatus() +
              " for Suite ID: SU-" +
              exec_obj.getCustomerId() +
              "" +
              exec_obj.getSuiteId() +
              "\n"
            );

          String spaces = " ";
          for (int i = 0; i < 27; i++) spaces += " ";
          taskContext
            .getBuildLogger()
            .addBuildLogEntry(
              spaces +
              "(Executed " +
              exec_obj.getExecutedTcs() +
              " of " +
              exec_obj.getTotalTcs() +
              " testcase(s), execution percentage: " +
              exec_obj.getExecPercent() +
              " %)"
            );

          taskContext
            .getBuildLogger()
            .addBuildLogEntry(
              "\n" +
              spaces +
              "(Failed " +
              exec_obj.getTcsFailed() +
              " of " +
              exec_obj.getTotalTcs() +
              " testcase(s), fail percentage: " +
              exec_obj.getFailPercent() +
              " %)"
            );

          taskContext
            .getBuildLogger()
            .addBuildLogEntry(
              "\n" +
              spaces +
              "(Threshold: " +
              exec_obj.getThreshold() +
              " % i.e. " +
              (
                (exec_obj.getThreshold() / 100.00) *
                Double.valueOf(exec_obj.getTotalTcs()).intValue() +
                " of " +
                exec_obj.getTotalTcs() +
                " testcase(s))\n"
              )
            );

          for (Object item : exec_obj.getResults()) {
            String tcCode = (((JSONObject) item).get("tcCode")).toString();

            String tcName = (((JSONObject) item).get("tcName")).toString();

            String result =
              (((JSONObject) item).get("result")).toString().toUpperCase();

            int totalSteps = Integer.parseInt(
              (((JSONObject) item).get("totalSteps")).toString()
            );

            taskContext
              .getBuildLogger()
              .addBuildLogEntry(
                spaces +
                tcCode +
                ": " +
                tcName +
                " | TESTCASE " +
                result +
                " (total steps: " +
                totalSteps +
                ")\n"
              );
          }

          if (exec_obj.getThreshold() <= exec_obj.getFailPercent()) {
            this.printLog(
                "\n" +
                ExecutionServicesImpl.getTimestamp() +
                "THRESHOLD REACHED!!!"
              );
            exec_obj.setExecStatus("FAILED");
            break;
          }
        }
      }

      exec_dto.checkExecStatus();
      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          "EXECUTION STATUS: Execution " +
          exec_obj.getExecStatus() +
          " for Suite ID: SU-" +
          exec_obj.getCustomerId() +
          "" +
          exec_obj.getSuiteId() +
          "\n"
        );

      String spaces = " ";
      for (int i = 0; i < 27; i++) spaces += " ";

      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          spaces +
          "(Executed " +
          exec_obj.getExecutedTcs() +
          " of " +
          exec_obj.getTotalTcs() +
          " testcase(s), execution percentage: " +
          exec_obj.getExecPercent() +
          " %)"
        );

      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          "\n" +
          spaces +
          "(Failed " +
          exec_obj.getTcsFailed() +
          " of " +
          exec_obj.getTotalTcs() +
          " testcase(s), fail percentage: " +
          exec_obj.getFailPercent() +
          " %)"
        );

      taskContext
        .getBuildLogger()
        .addBuildLogEntry(
          "\n" +
          spaces +
          "(Threshold: " +
          exec_obj.getThreshold() +
          " % i.e. " +
          (
            (exec_obj.getThreshold() / 100.00) *
            Double.valueOf(exec_obj.getTotalTcs()).intValue() +
            " of " +
            exec_obj.getTotalTcs() +
            " testcase(s))\n"
          )
        );

        
      for (Object item : exec_obj.getResults()) {
        String tcCode = (((JSONObject) item).get("tcCode")).toString();
        String tcName = (((JSONObject) item).get("tcName")).toString();
        String result =
          (((JSONObject) item).get("result")).toString().toUpperCase();
        int totalSteps = Integer.parseInt(
          (((JSONObject) item).get("totalSteps")).toString()
        );

        taskContext
          .getBuildLogger()
          .addBuildLogEntry(
            spaces +
            tcCode +
            ": " +
            tcName +
            " | TESTCASE " +
            result +
            " (total steps: " +
            totalSteps +
            ")\n"
          );
      }

      if (exec_obj.getThreshold() <= exec_obj.getFailPercent()) {
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
        taskContext
          .getBuildLogger()
          .addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() + "EXECUTION PASSED!!"
          );
        retFlag = true;
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

    return retFlag;
  }
}
