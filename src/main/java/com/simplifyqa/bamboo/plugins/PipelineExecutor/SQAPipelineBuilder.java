package io.jenkins.plugins.simplifyqa;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.simplifyqa.Services.ExecutionServices;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.json.simple.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class SQAPipelineBuilder extends Builder implements SimpleBuildStep {
    @SuppressWarnings("lgtm[jenkins/plaintext-storage]")
    

    // Constructor
    @DataBoundConstructor
    public SQAPipelineBuilder(String exec_token, String app_url, double threshold, boolean verbose) {

        if ((threshold < 1.00) || (threshold > 100.00)) threshold = 100.00;

        if (!(app_url.startsWith("http://") ^ app_url.startsWith("https://") ^ app_url.startsWith("localhost:")))
            app_url = "https://simplifyqa.app";

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
    @DataBoundSetter
    protected void setExec_token(String exec_token) {
        this.exec_token = exec_token;
    }

    @DataBoundSetter
    protected void setApp_url(String app_url) {

        if (!(app_url.startsWith("http://") ^ app_url.startsWith("https://") ^ app_url.startsWith("localhost:")))
            app_url = "https://simplifyqa.app";

        this.app_url = app_url;
    }

    @DataBoundSetter
    protected void setThreshold(double threshold) {

        if ((threshold < 1.00) || (threshold > 100.00)) threshold = 100.00;

        this.threshold = threshold;
    }

    @DataBoundSetter
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {

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
    }

    @Symbol("SQAPipelineExecutor")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @RequirePOST
        @SuppressWarnings("lgtm[jenkins/no-permission-check]")
        public FormValidation doCheckExec_token(@QueryParameter String value) throws IOException, ServletException {

            List<FormValidation> validationList = new ArrayList<>();
            validationList.add(
                    FormValidation.error(Messages.SQAPipelineBuilder_DescriptorImpl_errors_emptyExecToken()));
            validationList.add(FormValidation.validateRequired(value));

            if (value.length() < 1) return FormValidation.aggregate(validationList);

            if (value.length() != 88)
                return FormValidation.error(Messages.SQAPipelineBuilder_DescriptorImpl_errors_invalidExecToken());

            return FormValidation.ok("Valid Execution Token");
        }

        @RequirePOST
        @SuppressWarnings("lgtm[jenkins/no-permission-check]")
        public FormValidation doCheckApp_url(@QueryParameter String value)
                throws IOException, ServletException, URISyntaxException {

            List<FormValidation> validationList = new ArrayList<>();
            validationList.add(FormValidation.error(Messages.SQAPipelineBuilder_DescriptorImpl_errors_emptyAppUrl()));
            validationList.add(FormValidation.validateRequired(value));

            if (value.isBlank()) return FormValidation.aggregate(validationList);

            if (!(value.startsWith("http://") ^ value.startsWith("https://") ^ value.startsWith("localhost:")))
                return FormValidation.warning(Messages.SQAPipelineBuilder_DescriptorImpl_warnings_invalidAppUrl());

            if (ExecutionServices.getResponse(value, "GET", "").statusCode() != 200)
                return FormValidation.warning(Messages.SQAPipelineBuilder_DescriptorImpl_warnings_unreachableAppUrl());

            return FormValidation.ok("Valid App Url");
        }

        @SuppressWarnings("lgtm[jenkins/no-permission-check]")
        public FormValidation doCheckThreshold(@QueryParameter double value) throws IOException, ServletException {

            // return FormValidation.validateIntegerInRange(Double.toString(value), 0, 100);

            if ((value < 1.00) || (value > 100.00))
                return FormValidation.warning(Messages.SQAPipelineBuilder_DescriptorImpl_warnings_invalidThreshold());

            return FormValidation.ok("Valid Threshold");
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.SQAPipelineBuilder_DescriptorImpl_DisplayName();
        }
    }
}
