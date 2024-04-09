package com.simplifyqa.bamboo.plugins.api;

import com.atlassian.bamboo.build.logger.BuildLogger;
import java.io.IOException;
import org.json.simple.JSONArray;

// Execution Services Object
public abstract class Execution {

  protected String exec_token;
  protected String app_url = "https://simplifyqa.app";
  protected double threshold = 100.00;
  protected boolean verbose = false;

  protected long exec_id;
  protected int project_id;
  protected int customer_id;
  protected String user_name = "";
  protected int user_id;
  protected String auth_key = "";

  protected int suite_id = 0;
  protected double fail_percent = 0.00;
  protected double exec_percent = 0.00;
  protected int total_tcs = 0;
  protected int tcs_inprogress = 0;
  protected int executed_tcs = 0;
  protected int tcs_failed = 0;
  protected String report_url = "";
  protected JSONArray results;

  protected String exec_status = "UNINITIALIZED";

  protected BuildLogger logger;

  public Execution(
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

    logger.addBuildLogEntry(
      ExecutionServices.getTimestamp() +
      "**************************************START OF LOGS**************************************\n"
    );
    logger.addBuildLogEntry(
      ExecutionServices.getTimestamp() + "The Set Parameters are:"
    );

    String asterisks = "";
    for (int i = 0; i < 70; i++) asterisks += "*";

    logger.addBuildLogEntry(
      ExecutionServices.getTimestamp() +
      "Execution Token: " +
      asterisks +
      this.exec_token.substring(71, this.exec_token.length() - 1)
    );

    logger.addBuildLogEntry(
      ExecutionServices.getTimestamp() + "App Url: " + this.app_url
    );

    logger.addBuildLogEntry(
      ExecutionServices.getTimestamp() + "Threshold: " + this.threshold + "%"
    );

    logger.addBuildLogEntry(
      ExecutionServices.getTimestamp() + "Verbose: " + this.verbose
    );
  }

  // Getters (pre-connection)
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

  public BuildLogger getLogger() {
    return this.logger;
  }

  // Getters (post-connection)
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

  public String getAuthKey() {
    return this.auth_key;
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

  // Setters
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

  public void setAuthKey(String auth_key) {
    this.auth_key = auth_key;
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
}
