package com.simplifyqa.bamboo.plugins.api;

import com.simplifyqa.bamboo.plugins.impl.HttpResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.client.ClientProtocolException;

// Execution Object
public abstract class ExecutionServices {

  protected Execution exec_obj;

  protected String build_api = "/jenkinsSuiteExecution";
  protected String status_api = "/getJenkinsExecStatus";
  protected String kill_api = "/getsession/killExecutionReports";
  protected String exec_logs_api = "/executionlog";
  protected static String TIMESTAMP_FORMAT = "dd-MMM-yyyy HH:mm:ss";

  protected ExecutionServices() {
    this.exec_obj = null;
  }

  protected ExecutionServices(Execution exec_obj) {
    this.exec_obj = exec_obj;
    this.build_api = exec_obj.getApp_url() + this.build_api;
    this.status_api = exec_obj.getApp_url() + this.status_api;
    this.kill_api = exec_obj.getApp_url() + this.kill_api;
  }

  public void setExecObj(Execution exec_obj) {
    this.exec_obj = exec_obj;
    return;
  }

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

  public static String getTimestamp() {
    SimpleDateFormat formatter = new SimpleDateFormat(
      ExecutionServices.TIMESTAMP_FORMAT
    );
    Date date = new Date();
    return "[" + formatter.format(date) + " Hrs] ";
  }

  protected abstract HttpResponse makeHttpPostRequest(
    String url,
    String payload
  ) throws ClientProtocolException, IOException;

  protected abstract HttpResponse makeHttpGetRequest(String url)
    throws URISyntaxException, IOException, InterruptedException;

  protected abstract void printStats();

  protected abstract ExecutionState startExec() throws IOException;

  protected abstract ExecutionState checkExecStatus()
    throws InterruptedException, IOException;

  protected abstract boolean killExec() throws IOException;
}
