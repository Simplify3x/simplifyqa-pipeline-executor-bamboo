package com.simplifyqa.bamboo.plugins.impl;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.simplifyqa.bamboo.plugins.api.ExecutionServices;
import com.simplifyqa.bamboo.plugins.api.KillPayload;
import com.simplifyqa.bamboo.plugins.api.StatusPayload;
import com.simplifyqa.bamboo.plugins.api.TriggerPayload;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@ExportAsService({ ExecutionServices.class })
@Named("ExecutionServices")
public class ExecutionServicesImpl implements ExecutionServices {

  @ComponentImport
  private final ApplicationProperties applicationProperties;

  private ExecutionImpl exec_obj;

  @Inject
  public ExecutionServicesImpl(
    final ApplicationProperties applicationProperties
  ) {
    this.applicationProperties = applicationProperties;
  }

  public String getName() {
    if (null != applicationProperties) {
      return "myComponent:" + applicationProperties.getDisplayName();
    }

    return "myComponent";
  }

  public static String getTimestamp() {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    Date date = new Date();
    return "\n[" + formatter.format(date) + " Hrs] ";
  }

  public static HttpResponse makeHttpPostRequest(String url, String payload)
    throws URISyntaxException, IOException, InterruptedException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    int timeout = 5 * 60;
    RequestConfig config = RequestConfig
      .custom()
      .setConnectTimeout(timeout * 1000)
      .setConnectionRequestTimeout(timeout * 1000)
      .setSocketTimeout(timeout * 1000)
      .build();

    BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();

    CloseableHttpClient client = HttpClients
      .custom()
      .setConnectionManager(connectionManager)
      .build();

    HttpPost request = new HttpPost(url);
    StringEntity entity = new StringEntity(payload);
    entity.setContentType("application/json");
    request.setEntity(entity);
    request.setHeader("Content-Type", "application/json");
    request.setHeader("Authorization", ExecutionImpl.getAuthKey());

    return client.execute(request);
  }

  public static HttpResponse makeHttpGetRequest(String url)
    throws URISyntaxException, IOException, InterruptedException {
    int timeout = 5;
    RequestConfig config = RequestConfig
      .custom()
      .setConnectTimeout(timeout * 1000)
      .setConnectionRequestTimeout(timeout * 1000)
      .setSocketTimeout(timeout * 1000)
      .build();
    CloseableHttpClient client = HttpClientBuilder
      .create()
      .setDefaultRequestConfig(config)
      .build();

    HttpGet request = new HttpGet(url);

    return client.execute(request);
  }

  // Setters
  public void setExecObj(ExecutionImpl exec_obj) {
    this.exec_obj = exec_obj;
    return;
  }

  public boolean startExec() {
    boolean ret_flag = false;
    HttpResponse response = null;
    TriggerPayload payload = new TriggerPayload(this.exec_obj.getExec_token());
    try {
      response =
        ExecutionServicesImpl.makeHttpPostRequest(
          exec_obj.getBuildApi(),
          payload.getPayload()
        );

      switch (response.getStatusLine().getStatusCode()) {
        case 200:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution triggered."
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }

          this.exec_obj.setExecId(
              (long) (
                (JSONObject) new JSONParser().parse(response.toString())
              ).get("executionId")
            );

          this.exec_obj.setCustomerId(
              Integer.parseInt(
                (
                  (
                    (JSONObject) new JSONParser().parse(response.toString())
                  ).get("customerId")
                ).toString()
              )
            );

          this.exec_obj.setProjectId(
              Integer.parseInt(
                (
                  (
                    (JSONObject) new JSONParser().parse(response.toString())
                  ).get("projectId")
                ).toString()
              )
            );

          ExecutionImpl.setAuthKey(
            (String) (
              (JSONObject) new JSONParser().parse(response.toString())
            ).get("authKey")
          );

          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: INITIALIZING TESTCASES in the triggered suite"
          );

          ret_flag = true;
          break;
        case 400:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution did not get triggered."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: Invalid Execution token for the specified env: " +
            this.exec_obj.getApp_url()
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          break;
        case 403:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution did not get triggered."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: Invalid Execution token for the specified env: " +
            this.exec_obj.getApp_url()
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          break;
        case 500:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution did not get triggered."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: The cloud server or the local machine is unavailable for the specified env: " +
            this.exec_obj.getApp_url()
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          break;
        case 504:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution did not get triggered."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: The server gateway timed-out for the specified env: " +
            this.exec_obj.getApp_url()
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          break;
        default:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution did not get triggered."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: Something is critically broken on SQA Servers."
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          break;
      }

      return ret_flag;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      exec_obj.logger.addBuildLogEntry(
        ExecutionServicesImpl.getTimestamp() +
        "EXECUTION STATUS: No response received. Is the server down?"
      );

      e.printStackTrace();
      return ret_flag;
    }
  }

  public String checkExecStatus() {
    HttpResponse response = null;
    StringBuilder toPrint = new StringBuilder();
    int failsafe_counter = 60; // The failsafe counter is used to ensure the app to waits until the
    // execution
    // is properly started or response is properly determined

    StatusPayload payload = new StatusPayload(
      this.exec_obj.getExecId(),
      this.exec_obj.getCustomerId(),
      this.exec_obj.getProjectId()
    );
    try {
      response =
        ExecutionServicesImpl.makeHttpPostRequest(
          exec_obj.getBuildApi(),
          payload.getPayload()
        );

      while (
        !(
          Boolean.valueOf(
            ((JSONObject) new JSONParser().parse(response.toString())).get(
                "success"
              )
              .toString()
          )
        ) &&
        (failsafe_counter > 0)
      ) {
        response =
          ExecutionServicesImpl.makeHttpPostRequest(
            exec_obj.getBuildApi(),
            payload.getPayload()
          );
        Thread.sleep(5000);
        failsafe_counter--;
      }

      switch (response.getStatusLine().getStatusCode()) {
        case 200:
          // exec_obj.logger.addBuildLogEntry(ExecutionServicesImpl.getTimestamp() + "EXECUTION STATUS: Status
          // code "
          // + response.getStatusLine().getStatusCode() + ", Execution Status fetched successfully.");

          JSONObject dataObj = (JSONObject) new JSONParser()
            .parse(
              (
                (JSONObject) new JSONParser()
                  .parse(
                    (
                      (JSONObject) new JSONParser().parse(response.toString())
                    ).get("data")
                      .toString()
                  )
              ).get("data")
                .toString()
            );

          this.exec_obj.setTcsFailed(0);
          for (Object item : (JSONArray) dataObj.get("result")) if (
            ((JSONObject) item).get("result")
              .toString()
              .equalsIgnoreCase("FAILED")
          ) this.exec_obj.setTcsFailed(this.exec_obj.getTcsFailed() + 1);

          this.exec_obj.setExecutedTcs(0);
          for (Object item : (JSONArray) dataObj.get("result")) if (
            (
              ((JSONObject) item).get("result")
                .toString()
                .equalsIgnoreCase("PASSED")
            ) ||
            (
              ((JSONObject) item).get("result")
                .toString()
                .equalsIgnoreCase("FAILED")
            )
          ) this.exec_obj.setExecutedTcs(this.exec_obj.getExecutedTcs() + 1);

          this.exec_obj.setResults(((JSONArray) dataObj.get("result")));
          this.exec_obj.setTotalTcs(
              Integer.parseInt(dataObj.get("totalTestcases").toString())
            );
          this.exec_obj.setSuiteId(
              Integer.parseInt(dataObj.get("suiteId").toString())
            );
          this.exec_obj.setReportUrl((String) dataObj.get("reporturl"));
          this.exec_obj.setExecStatus((String) dataObj.get("execution"));
          this.exec_obj.setUserId(
              Integer.parseInt(dataObj.get("userId").toString())
            );
          this.exec_obj.setUserName((String) dataObj.get("username"));
          this.exec_obj.setFailPercent();
          this.exec_obj.setExecPercent();

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          break;
        case 400:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution Status could not be fetched."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: Logout and login again, Invalid Execution token for the specified env: " +
            this.exec_obj.getApp_url()
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          this.exec_obj.setExecStatus("FAILED");
          break;
        case 403:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution Status could not be fetched."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: Logout and login again, Invalid Authorization token for the specified env: " +
            this.exec_obj.getApp_url()
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          this.exec_obj.setExecStatus("FAILED");
          break;
        case 500:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution Status could not be fetched."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: The cloud server or the local machine is unavailable for the specified env: " +
            this.exec_obj.getApp_url()
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          this.exec_obj.setExecStatus("FAILED");
          break;
        case 504:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution Status could not be fetched."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: The server gateway timed-out for the specified env: " +
            this.exec_obj.getApp_url()
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          this.exec_obj.setExecStatus("FAILED");
          break;
        default:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution Status could not be fetched."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: Something is critically broken on SQA Servers."
          );

          if (exec_obj.getVerbose()) {
            this.exec_obj.setReqBody(
                ExecutionServicesImpl.getTimestamp() +
                "REQUEST BODY: " +
                payload.getPayload()
              );
            this.exec_obj.setRespBody(
                ExecutionServicesImpl.getTimestamp() +
                "RESPONSE BODY: " +
                response.toString()
              );
          }
          this.exec_obj.setExecStatus("FAILED");
          break;
      }

      this.exec_obj.addLogs(toPrint.toString());
      this.exec_obj.getBuildLogger().addBuildLogEntry(toPrint.toString());
      toPrint.delete(0, toPrint.length());

      return this.exec_obj.getExecStatus();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      exec_obj.logger.addBuildLogEntry(
        ExecutionServicesImpl.getTimestamp() +
        "EXECUTION STATUS: No response received. Is the server down?"
      );
      this.exec_obj.addLogs(toPrint.toString());
      this.exec_obj.getBuildLogger().addBuildLogEntry(toPrint.toString());
      toPrint.delete(0, toPrint.length());

      e.printStackTrace();
      return "FAILED";
    }
  }

  public boolean killExec() {
    boolean ret_flag = false;
    HttpResponse response = null;

    KillPayload payload = new KillPayload(
      this.exec_obj.getCustomerId(),
      this.exec_obj.getExecId(),
      this.exec_obj.getUserId(),
      this.exec_obj.getUserName()
    );
    try {
      response =
        ExecutionServicesImpl.makeHttpPostRequest(
          exec_obj.getKillApi(),
          payload.getPayload()
        );
      switch (response.getStatusLine().getStatusCode()) {
        case 200:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Execution Killed Successfully."
          );
          ret_flag = true;
          break;
        case 400:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Failed to kill execiution."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() + this.exec_obj.getApp_url()
          );
          this.exec_obj.setExecStatus("FAILED");
          break;
        case 403:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Failed to kill execiution."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: Logout and login again, Invalid Authorization token for the specified env: " +
            this.exec_obj.getApp_url()
          );
          this.exec_obj.setExecStatus("FAILED");
          break;
        case 500:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Failed to kill execiution."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: The Pipeline Token is invalid for the specified env: " +
            this.exec_obj.getApp_url()
          );
          this.exec_obj.setExecStatus("FAILED");
          break;
        case 504:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Failed to kill execiution."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: The server gateway timed-out for the specified env: " +
            this.exec_obj.getApp_url()
          );
          this.exec_obj.setExecStatus("FAILED");
          break;
        default:
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Status code " +
            response.getStatusLine().getStatusCode() +
            ", Failed to kill execiution."
          );
          exec_obj.logger.addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "REASON OF FAILURE: Something is critically broken on SQA Servers."
          );
          this.exec_obj.setExecStatus("FAILED");
          break;
      }

      if (exec_obj.getVerbose()) {
        this.exec_obj.setReqBody(
            ExecutionServicesImpl.getTimestamp() +
            "REQUEST BODY: " +
            payload.getPayload()
          );
        this.exec_obj.setRespBody(
            ExecutionServicesImpl.getTimestamp() +
            "RESPONSE BODY: " +
            response.toString()
          );
      }

      return ret_flag;
    } catch (Exception e) {
      exec_obj.logger.addBuildLogEntry(
        ExecutionServicesImpl.getTimestamp() +
        "EXECUTION STATUS: No response received. Is the server down?"
      );
      if (exec_obj.getVerbose()) {
        this.exec_obj.setReqBody(
            ExecutionServicesImpl.getTimestamp() +
            "REQUEST BODY: " +
            payload.getPayload()
          );
        this.exec_obj.setRespBody(
            ExecutionServicesImpl.getTimestamp() +
            "RESPONSE BODY: " +
            response.toString()
          );
      }

      e.printStackTrace();
      return ret_flag;
    }
  }
}
