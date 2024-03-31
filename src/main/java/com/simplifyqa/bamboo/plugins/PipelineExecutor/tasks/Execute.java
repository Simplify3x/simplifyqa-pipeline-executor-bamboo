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

    ExecutionServices exec_dto = new ExecutionServices();

    if (this.exec_token.length() != 88) {
        run.setResult(Result.NOT_BUILT);
        exec_dto.printLog(ExecutionServices.getTimestamp() + "*".repeat(51) + "EOF" + "*".repeat(51) + "\n");
        return;
    }

    ExecutionImpl exec_obj =
            new ExecutionImpl(this.exec_token, this.app_url.toLowerCase(), this.threshold, this.verbose, listener);
    exec_dto.setExecObj(exec_obj);
    run.addAction(exec_obj);

    if (!exec_dto.startExec()) run.setResult(Result.NOT_BUILT);
    else {
        int executed = exec_obj.getExecutedTcs();

        if (exec_obj.getVerbose()) exec_dto.printLog(exec_obj.getReqBody() + exec_obj.getRespBody() + "\n");

        while ((exec_dto.checkExecStatus().equalsIgnoreCase("INPROGRESS"))
                && (exec_obj.getThreshold() > exec_obj.getFailPercent())) {

            if (executed < exec_obj.getExecutedTcs()) {
                executed++;
                exec_dto.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Execution "
                        + exec_obj.getExecStatus() + " for Suite ID: SU-" + exec_obj.getCustomerId() + ""
                        + exec_obj.getSuiteId() + "\n");

                exec_dto.printLog(
                        " ".repeat(27) + "(Executed " + exec_obj.getExecutedTcs() + " of " + exec_obj.getTotalTcs()
                                + " testcase(s), execution percentage: " + exec_obj.getExecPercent() + " %)");

                exec_dto.printLog("\n" + " ".repeat(27) + "(Failed " + exec_obj.getTcsFailed() + " of "
                        + exec_obj.getTotalTcs() + " testcase(s), fail percentage: " + exec_obj.getFailPercent()
                        + " %)");

                exec_dto.printLog("\n" + " ".repeat(27) + "(Threshold: " + exec_obj.getThreshold() + " % i.e. "
                        + ((exec_obj.getThreshold() / 100.00)
                                        * Double.valueOf(exec_obj.getTotalTcs())
                                                .intValue()
                                + " of " + exec_obj.getTotalTcs() + " testcase(s))\n"));

                for (Object item : exec_obj.getResults()) {
                    String tcCode = (((JSONObject) item).get("tcCode")).toString();
                    String tcName = (((JSONObject) item).get("tcName")).toString();
                    String result =
                            (((JSONObject) item).get("result")).toString().toUpperCase();
                    int totalSteps = Integer.parseInt((((JSONObject) item).get("totalSteps")).toString());

                    exec_dto.printLog(" ".repeat(27) + tcCode + ": " + tcName + " | TESTCASE " + result
                            + " (total steps: " + totalSteps + ")\n");
                }

                if (exec_obj.getVerbose()) exec_dto.printLog(exec_obj.getReqBody() + exec_obj.getRespBody() + "\n");

                if (exec_obj.getThreshold() <= exec_obj.getFailPercent()) {
                    exec_dto.printLog("\n" + ExecutionServices.getTimestamp() + "THRESHOLD REACHED!!!");
                    exec_obj.setExecStatus("FAILED");
                    break;
                }
            }
        }

        exec_dto.checkExecStatus();
        exec_dto.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Execution "
                + exec_obj.getExecStatus() + " for Suite ID: SU-" + exec_obj.getCustomerId() + ""
                + exec_obj.getSuiteId() + "\n");

        exec_dto.printLog(
                " ".repeat(27) + "(Executed " + exec_obj.getExecutedTcs() + " of " + exec_obj.getTotalTcs()
                        + " testcase(s), execution percentage: " + exec_obj.getExecPercent() + " %)");

        exec_dto.printLog("\n" + " ".repeat(27) + "(Failed " + exec_obj.getTcsFailed() + " of "
                + exec_obj.getTotalTcs() + " testcase(s), fail percentage: " + exec_obj.getFailPercent() + " %)");

        exec_dto.printLog("\n" + " ".repeat(27) + "(Threshold: " + exec_obj.getThreshold() + " % i.e. "
                + ((exec_obj.getThreshold() / 100.00)
                                * Double.valueOf(exec_obj.getTotalTcs()).intValue() + " of "
                        + exec_obj.getTotalTcs() + " testcase(s))\n"));

        for (Object item : exec_obj.getResults()) {
            String tcCode = (((JSONObject) item).get("tcCode")).toString();
            String tcName = (((JSONObject) item).get("tcName")).toString();
            String result = (((JSONObject) item).get("result")).toString().toUpperCase();
            int totalSteps = Integer.parseInt((((JSONObject) item).get("totalSteps")).toString());

            exec_dto.printLog(" ".repeat(27) + tcCode + ": " + tcName + " | TESTCASE " + result + " (total steps: "
                    + totalSteps + ")\n");
        }

        if (exec_obj.getVerbose()) exec_dto.printLog(exec_obj.getReqBody() + exec_obj.getRespBody() + "\n");

        if (exec_obj.getThreshold() <= exec_obj.getFailPercent()) {

            exec_dto.printLog(ExecutionServices.getTimestamp() + "EXECUTION FAILED!!");

            if (exec_dto.killExec())
                exec_dto.printLog(ExecutionServices.getTimestamp()
                        + "EXECUTION STATUS: SUCCESSFUL to explicitly kill the execution!\n");
            else
                exec_dto.printLog(ExecutionServices.getTimestamp()
                        + "EXECUTION STATUS: FAILED to explicitly kill the execution!\n");

            if (exec_obj.getVerbose()) exec_dto.printLog(exec_obj.getReqBody() + exec_obj.getRespBody() + "\n");

            run.setResult(Result.FAILURE);
        } else {
            exec_dto.printLog(ExecutionServices.getTimestamp() + "EXECUTION PASSED!!");
            run.setResult(Result.SUCCESS);
        }

        exec_dto.printLog(ExecutionServices.getTimestamp() + "REPORT URL: " + exec_obj.getReportUrl() + "\n");
    }

    exec_dto.printLog(ExecutionServices.getTimestamp() + "*".repeat(51) + "EOF" + "*".repeat(51) + "\n");
    return;
    return TaskResultBuilder.create(taskContext).success().build();
  }
}
