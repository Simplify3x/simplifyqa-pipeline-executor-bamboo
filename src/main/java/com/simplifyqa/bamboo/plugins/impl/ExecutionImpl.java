package com.simplifyqa.bamboo.plugins.impl;

import com.atlassian.bamboo.build.logger.BuildLogger;
import java.io.IOException;
import java.text.DecimalFormat;
import org.json.simple.JSONArray;

public class ExecutionImpl {

  private String exec_token;

  private String app_url = "https://simplifyqa.app";
  private double threshold = 100.00;
  private boolean verbose = false;

  private String build_api = "/jenkinsSuiteExecution";
  private String status_api = "/getJenkinsExecStatus";
  private String kill_api = "/getsession/killExecutionReports";
  private String exec_logs_api = "/executionlog";

  // These variables are initialized from the internal APIs of SQA
  private long exec_id;
  private int project_id;
  private int customer_id;
  private String user_name = "";
  private int user_id;
  private static String auth_key = "";

  // Post-Execution trigger Data Members
  private int suite_id = 0;
  private double fail_percent = 0.00;
  private double exec_percent = 0.00;
  private int total_tcs = 0;
  private int tcs_inprogress = 0;
  private int executed_tcs = 0;
  private int tcs_failed = 0;
  private String report_url = "";
  private JSONArray results;

  private String req_body;
  private String resp_body;

  private String exec_status = "UNINITIALIZED";

  private StringBuilder logs;
  private String toPrint;

  BuildLogger logger;

  public ExecutionImpl(
    String exec_token,
    String app_url,
    double threshold,
    boolean verbose,
    BuildLogger logger
  ) throws IOException {
    this.exec_token = exec_token;
    this.app_url = app_url;
    this.threshold = threshold;
    this.verbose = verbose;
    this.logger = logger;

    this.build_api = app_url + this.build_api;
    this.status_api = app_url + this.status_api;
    this.kill_api = app_url + this.kill_api;
    this.exec_logs_api = app_url + this.exec_logs_api;

    // this.logs = new StringBuilder(ExecutionServices.getBanner());
    this.logs = new StringBuilder();

    this.toPrint =
      ExecutionServicesImpl.getTimestamp() +
      "**************************************START OF LOGS**************************************\n";
    this.toPrint +=
      ExecutionServicesImpl.getTimestamp() + "The Set Parameters are:";

    String asterisks = "";
    for (int i = 0; i < 70; i++) asterisks += "*";

    this.toPrint +=
      ExecutionServicesImpl.getTimestamp() +
      "Execution Token: " +
      asterisks +
      this.exec_token.substring(71, this.exec_token.length() - 1);

    this.toPrint +=
      ExecutionServicesImpl.getTimestamp() + "App Url: " + this.app_url;

    this.toPrint +=
      ExecutionServicesImpl.getTimestamp() +
      "Threshold: " +
      this.threshold +
      "%";

    this.toPrint +=
      ExecutionServicesImpl.getTimestamp() + "Verbose: " + this.verbose;

    this.logs.append(toPrint);
    this.logger.addBuildLogEntry(toPrint);
  }

  // Getters (pre-connection)
  public String getLogs() {
    return this.logs.toString();
  }

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

  public BuildLogger getBuildLogger() {
    return this.logger;
  }

  // Getters (post-connection)
  public String getBuildApi() {
    return this.build_api;
  }

  public String getStatusApi() {
    return this.status_api;
  }

  public String getKillApi() {
    return this.kill_api;
  }

  public String getExecLogsApi() {
    return this.exec_logs_api;
  }

  public long getExecId() {
    return this.exec_id;
  }

  public int getProjectId() {
    return this.project_id;
  }

  public int getCustomerId() {
    return this.customer_id;
  }

  public String getUserName() {
    return this.user_name;
  }

  public int getUserId() {
    return this.user_id;
  }

  public static String getAuthKey() {
    return ExecutionImpl.auth_key;
  }

  public int getSuiteId() {
    return this.suite_id;
  }

  public double getFailPercent() {
    return this.fail_percent;
  }

  public double getExecPercent() {
    return this.exec_percent;
  }

  public int getTotalTcs() {
    return this.total_tcs;
  }

  public int getTcsInprogress() {
    return this.tcs_inprogress;
  }

  public int getExecutedTcs() {
    return this.executed_tcs;
  }

  public int getTcsFailed() {
    return this.tcs_failed;
  }

  public String getReportUrl() {
    return this.report_url;
  }

  public JSONArray getResults() {
    return this.results;
  }

  public String getExecStatus() {
    return this.exec_status;
  }

  public String getReqBody() {
    return this.req_body;
  }

  public String getRespBody() {
    return this.resp_body;
  }

  // Setters
  public void addLogs(String toAdd) {
    this.logs.append(toAdd);
  }

  public void setExecId(long exec_id) {
    this.exec_id = exec_id;
  }

  public void setProjectId(int project_id) {
    this.project_id = project_id;
  }

  public void setCustomerId(int customer_id) {
    this.customer_id = customer_id;
  }

  public void setUserName(String user_name) {
    this.user_name = user_name;
  }

  public void setUserId(int user_id) {
    this.user_id = user_id;
  }

  public static void setAuthKey(String auth_key) {
    ExecutionImpl.auth_key = auth_key;
  }

  public void setSuiteId(int suite_id) {
    this.suite_id = suite_id;
  }

  public void setFailPercent() {
    double failPercentage = (double) this.tcs_failed / this.total_tcs * 100.0;
    this.fail_percent =
      Double.parseDouble(String.format("%.2f", failPercentage));
  }

  public void setExecPercent() {
    double executedPercentage = (double) this.executed_tcs /
    this.total_tcs *
    100.0;
    this.exec_percent =
      Double.parseDouble(String.format("%.2f", executedPercentage));
  }

  public void setTotalTcs(int total_tcs) {
    this.total_tcs = total_tcs;
  }

  public void setTcsInprogress(int tcs_inprogress) {
    this.tcs_inprogress = tcs_inprogress;
  }

  public void setExecutedTcs(int executed_tcs) {
    this.executed_tcs = executed_tcs;
  }

  public void setTcsFailed(int tcs_failed) {
    this.tcs_failed = tcs_failed;
  }

  public void setReportUrl(String report_url) {
    this.report_url = report_url;
  }

  public void setResults(JSONArray results) {
    this.results = results;
  }

  public void setExecStatus(String exec_status) {
    this.exec_status = exec_status.toUpperCase();
  }

  public void setReqBody(String req_body) {
    this.req_body = req_body;
  }

  public void setRespBody(String resp_body) {
    this.resp_body = resp_body;
  }
}
