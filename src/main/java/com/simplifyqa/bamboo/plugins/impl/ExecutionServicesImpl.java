package com.simplifyqa.bamboo.plugins.impl;

import com.simplifyqa.bamboo.plugins.api.Execution;
import com.simplifyqa.bamboo.plugins.api.ExecutionServices;
import com.simplifyqa.bamboo.plugins.api.ExecutionState;
import com.simplifyqa.bamboo.plugins.api.payloads.GenericPayload;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ExecutionServicesImpl extends ExecutionServices {

  public ExecutionServicesImpl() {
    super();
  }

  public ExecutionServicesImpl(Execution exec_dao) {
    super(exec_dao);
  }

  private static final int CONNECTION_TIMEOUT = 300000; // 5 minutes

  @Override
  public HttpResponse makeHttpGetRequest(String url) throws IOException {
    HttpURLConnection connection = null;
    try {
      URL requestUrl = new URL(url);
      connection = (HttpURLConnection) requestUrl.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setConnectTimeout(CONNECTION_TIMEOUT);
      connection.setReadTimeout(CONNECTION_TIMEOUT);

      int responseCode = connection.getResponseCode();
      StringBuilder response = new StringBuilder();

      try (
        BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream(), "utf-8")
        )
      ) {
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line.trim());
        }
      }

      return new HttpResponse(responseCode, response.toString());
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  @Override
  public HttpResponse makeHttpPostRequest(String url, String body)
    throws IOException {
    HttpURLConnection connection = null;
    try {
      URL requestUrl = new URL(url);
      connection = (HttpURLConnection) requestUrl.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Authorization" ,this.exec_obj.getAuthKey());
      connection.setConnectTimeout(CONNECTION_TIMEOUT);
      connection.setReadTimeout(CONNECTION_TIMEOUT);
      connection.setDoOutput(true);

      if (body != null && !body.isEmpty()) {
        try (OutputStream outputStream = connection.getOutputStream()) {
          byte[] input = body.getBytes("utf-8");
          outputStream.write(input, 0, input.length);
        }
      }

      int responseCode = connection.getResponseCode();
      StringBuilder response = new StringBuilder();

      try (
        BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream(), "utf-8")
        )
      ) {
        String line;
        while ((line = reader.readLine()) != null) response.append(line.trim());
      }

      return new HttpResponse(responseCode, response.toString());
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  @Override
  public void printStats() {
    this.exec_obj.getLogger()
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
    this.exec_obj.getLogger()
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

    this.exec_obj.getLogger()
      .addBuildLogEntry(
        spaces +
        "(Failed " +
        exec_obj.getTcsFailed() +
        " of " +
        exec_obj.getTotalTcs() +
        " testcase(s), fail percentage: " +
        exec_obj.getFailPercent() +
        " %)"
      );

    this.exec_obj.getLogger()
      .addBuildLogEntry(
        spaces +
        "(Threshold: " +
        exec_obj.getThreshold() +
        " % i.e. " +
        (
          exec_obj.getTcsFailed() +
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

      this.exec_obj.getLogger()
        .addBuildLogEntry(
          spaces +
          tcCode +
          ": " +
          tcName +
          " | TESTCASE " +
          result +
          " (total steps: " +
          totalSteps +
          ")"
        );
    }

    if (this.exec_obj.getVerbose()) {
      this.exec_obj.getLogger()
        .addBuildLogEntry(
          ExecutionServices.getTimestamp() +
          "API CALLED: " +
          this.exec_obj.getCalledAPI()
        );
      this.exec_obj.getLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          "REQUEST BODY: " +
          this.exec_obj.getReqBody()
        );
      this.exec_obj.getLogger()
        .addBuildLogEntry(
          ExecutionServicesImpl.getTimestamp() +
          "RESPONSE BODY: " +
          this.exec_obj.getRespBody()
        );
    }
  }

  @Override
  public ExecutionState startExec() {
    if (this.exec_obj == null) {
      this.exec_obj.setExecStatus(ExecutionState.FAILED);
      return this.exec_obj.getExecStatus();
    } else {
      GenericPayload payload = GenericPayload.createTriggerPayload(
        this.exec_obj
      );
      HttpResponse response;
      try {
        response =
          this.makeHttpPostRequest(this.build_api, payload.getPayload());
      } catch (IOException e) {
        e.printStackTrace();
        this.exec_obj.setExecStatus(ExecutionState.FAILED);
        return this.exec_obj.getExecStatus();
      }

      if (response == null) {
        this.exec_obj.setExecStatus(ExecutionState.FAILED);
        return this.exec_obj.getExecStatus();
      } else {
        switch (response.getResponseCode()) {
          case 200:
            try {
              this.exec_obj.setExecId(
                  (long) (
                    (JSONObject) new JSONParser()
                      .parse(response.getResponseBody())
                  ).get("executionId")
                );

              this.exec_obj.setCustomerId(
                  Integer.parseInt(
                    (
                      (
                        (JSONObject) new JSONParser()
                          .parse(response.getResponseBody())
                      ).get("customerId")
                    ).toString()
                  )
                );

              this.exec_obj.setProjectId(
                  Integer.parseInt(
                    (
                      (
                        (JSONObject) new JSONParser()
                          .parse(response.getResponseBody())
                      ).get("projectId")
                    ).toString()
                  )
                );

              this.exec_obj.setAuthKey(
                  (String) (
                    (JSONObject) new JSONParser()
                      .parse(response.getResponseBody())
                  ).get("authKey")
                );
              // this.exec_obj.getLogger()
              //   .addBuildLogEntry(
              //     "EXEC_ID: " +
              //     this.exec_obj.getExecId() +
              //     "CUST_ID: " +
              //     this.exec_obj.getCustomerId() +
              //     "PROJ_ID: " +
              //     this.exec_obj.getProjectId() +
              //     "AUTH_KEY: " +
              //     this.exec_obj.getAuthKey()
              //   );
            } catch (ParseException PE) {
              PE.printStackTrace();
              return ExecutionState.FAILED;
            }

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServices.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution triggered."
              );
            this.exec_obj.setExecStatus(ExecutionState.INPROGRESS);
            break;
          case 400:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServices.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution did not get triggered."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServices.getTimestamp() +
                "REASON OF FAILURE: Invalid Execution token for the specified env: " +
                this.exec_obj.getApp_url()
              );

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.setCalledAPI(this.build_api);
              this.exec_obj.setReqBody(payload.getPayload());
              this.exec_obj.setRespBody(response.getResponseBody());
            }
            this.exec_obj.setExecStatus(ExecutionState.FAILED);
            break;
          case 403:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution did not get triggered."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: Invalid Execution token for the specified env: " +
                this.exec_obj.getApp_url()
              );
            this.exec_obj.setExecStatus(ExecutionState.FAILED);
            break;
          case 500:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution did not get triggered."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: The cloud server or the local machine is unavailable for the specified env: " +
                this.exec_obj.getApp_url()
              );
            this.exec_obj.setExecStatus(ExecutionState.FAILED);
            break;
          case 504:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution did not get triggered."
              );
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: The server gateway timed-out for the specified env: " +
                this.exec_obj.getApp_url()
              );
            this.exec_obj.setExecStatus(ExecutionState.FAILED);
            break;
          default:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution did not get triggered."
              );
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: An unclassified error has occurred." +
                this.exec_obj.getApp_url()
              );
            this.exec_obj.setExecStatus(ExecutionState.FAILED);
            break;
        }

        if (this.exec_obj.getVerbose()) {
          this.exec_obj.setCalledAPI(this.build_api);
          this.exec_obj.setReqBody(payload.getPayload());
          this.exec_obj.setRespBody(response.getResponseBody());
        }

        return this.exec_obj.getExecStatus();
      }
    }
  }

  @Override
  public ExecutionState checkExecStatus() {
    if (this.exec_obj == null) {
      this.exec_obj.setExecStatus(ExecutionState.FAILED);
      return this.exec_obj.getExecStatus();
    } else {
      int failsafe_counter = 60;
      GenericPayload payload = GenericPayload.createStatusPayload(
        this.exec_obj
      );

      HttpResponse response = null;

      do {
        try {
          response =
            this.makeHttpPostRequest(this.status_api, payload.getPayload());
        } catch (IOException IOE) {
          this.exec_obj.getLogger()
            .addBuildLogEntry(
              ExecutionServices.getTimestamp() +
              "EXECUTION STATUS: Execution Status could not be fetched."
            );
          if (this.exec_obj.getVerbose()) {
            this.exec_obj.setCalledAPI(this.build_api);
            this.exec_obj.setReqBody(payload.getPayload());
            this.exec_obj.setRespBody("");
          }
          this.exec_obj.setExecStatus(ExecutionState.INPROGRESS);
          IOE.printStackTrace();
        }

        try {
          JSONObject jsonResponse = (JSONObject) new JSONParser()
            .parse(response.getResponseBody());
          if (Boolean.valueOf(jsonResponse.get("success").toString())) break; // Break the loop if success is true
        } catch (ParseException PE) {
          this.exec_obj.getLogger()
            .addBuildLogEntry(
              ExecutionServices.getTimestamp() +
              "EXECUTION STATUS: Execution Status could not be fetched."
            );
          if (this.exec_obj.getVerbose()) {
            this.exec_obj.setCalledAPI(this.build_api);
            this.exec_obj.setReqBody(payload.getPayload());
            this.exec_obj.setRespBody("");
          }
          this.exec_obj.setExecStatus(ExecutionState.INPROGRESS);
          PE.printStackTrace();
        } catch (NullPointerException NPE) {
          this.exec_obj.getLogger()
            .addBuildLogEntry(
              ExecutionServices.getTimestamp() +
              "EXECUTION STATUS: Execution Status could not be fetched."
            );
          if (this.exec_obj.getVerbose()) {
            this.exec_obj.setCalledAPI(this.build_api);
            this.exec_obj.setReqBody(payload.getPayload());
            this.exec_obj.setRespBody("");
          }
          this.exec_obj.setExecStatus(ExecutionState.INPROGRESS);
          NPE.printStackTrace();
        }

        try {
          Thread.sleep(5000);
        } catch (InterruptedException IE) {
          this.exec_obj.getLogger()
            .addBuildLogEntry(
              ExecutionServices.getTimestamp() +
              "EXECUTION STATUS: Execution Status could not be fetched."
            );

          if (this.exec_obj.getVerbose()) {
            this.exec_obj.setCalledAPI(this.build_api);
            this.exec_obj.setReqBody(payload.getPayload());
            this.exec_obj.setRespBody("");
          }
          this.exec_obj.setExecStatus(ExecutionState.INPROGRESS);
          IE.printStackTrace();
        }

        failsafe_counter--;
      } while (failsafe_counter > 0);

      if (response == null) {
        this.exec_obj.setExecStatus(ExecutionState.FAILED);
        return this.exec_obj.getExecStatus();
      } else {
        switch (response.getResponseCode()) {
          case 200:
            JSONObject dataObj;
            try {
              dataObj =
                (JSONObject) new JSONParser()
                  .parse(
                    (
                      (JSONObject) new JSONParser()
                        .parse(
                          (
                            (JSONObject) new JSONParser()
                              .parse(response.getResponseBody())
                          ).get("data")
                            .toString()
                        )
                    ).get("data")
                      .toString()
                  );
            } catch (ParseException PE) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServices.getTimestamp() +
                  "EXECUTION STATUS: Status code " +
                  response.getResponseCode() +
                  ", Execution Status fetched successfully."
                );

              if (this.exec_obj.getVerbose()) {
                this.exec_obj.setCalledAPI(this.build_api);
                this.exec_obj.setReqBody(payload.getPayload());
                this.exec_obj.setRespBody("");
              }

              PE.printStackTrace();

              this.exec_obj.setExecStatus(ExecutionState.INPROGRESS);
              return this.exec_obj.getExecStatus();
            }

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

            this.exec_obj.setUserId(
                Integer.parseInt(dataObj.get("userId").toString())
              );
            this.exec_obj.setUserName((String) dataObj.get("username"));
            this.exec_obj.setFailPercent();
            this.exec_obj.setExecPercent();
            // this.exec_obj.getLogger()
            //   .addBuildLogEntry(
            //     ExecutionServices.getTimestamp() +
            //     "EXECUTION STATUS: Status code " +
            //     response.getResponseCode() +
            //     ", Execution Status fetched successfully."
            //   );
            this.exec_obj.setExecStatus(
                ExecutionState.setState(
                  String.valueOf(dataObj.get("execution"))
                )
              );
            break;
          case 400:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution Status could not be fetched."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: Logout and login again, Invalid Execution token for the specified env: " +
                this.exec_obj.getApp_url()
              );
            break;
          case 403:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution Status could not be fetched."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: Logout and login again, Invalid Authorization token for the specified env: " +
                this.exec_obj.getApp_url()
              );
            break;
          case 500:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution Status could not be fetched."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: The cloud server or the local machine is unavailable for the specified env: " +
                this.exec_obj.getApp_url()
              );
            break;
          case 504:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution Status could not be fetched."
              );
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: The server gateway timed-out for the specified env: " +
                this.exec_obj.getApp_url()
              );
            break;
          default:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution Status could not be fetched."
              );
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: An unclassified error has occurred." +
                this.exec_obj.getApp_url()
              );
            break;
        }

        if (this.exec_obj.getVerbose()) {
          this.exec_obj.setCalledAPI(this.build_api);
          this.exec_obj.setReqBody(payload.getPayload());
          this.exec_obj.setRespBody(response.getResponseBody());
        }

        return this.exec_obj.getExecStatus();
      }
    }
  }

  @Override
  public boolean killExec() {
    if (this.exec_obj == null) return false; else {
      GenericPayload payload = GenericPayload.createKillPayload(this.exec_obj);
      HttpResponse response = null;
      try {

        response =
          this.makeHttpPostRequest(this.kill_api, payload.getPayload());
      } catch (IOException e) {
        this.exec_obj.getLogger()
          .addBuildLogEntry(
            ExecutionServicesImpl.getTimestamp() +
            "EXECUTION STATUS: Failed to kill execution."
            );
          
        e.printStackTrace();
      }

      if (response == null) return false; else {
        boolean ret_flag = false;
        switch (response.getResponseCode()) {
          case 200:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution Killed Successfully."
              );
            ret_flag = true;
            break;
          case 400:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execution."
              );
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: Logout and login again, Invalid Authorization token for the specified env: " +
                this.exec_obj.getApp_url()
              );
            break;
          case 403:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execution."
              );
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: Logout and login again, Invalid Authorization token for the specified env: " +
                this.exec_obj.getApp_url()
              );
            break;
          case 500:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execution."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: The Pipeline Token is invalid for the specified env: " +
                this.exec_obj.getApp_url()
              );
            break;
          case 504:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execution."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: The server gateway timed-out for the specified env: " +
                this.exec_obj.getApp_url()
              );
            break;
          default:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execution."
              );
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: An unclassified error has occurred." +
                this.exec_obj.getApp_url()
              );
            break;
        }

        if (this.exec_obj.getVerbose()) {
          this.exec_obj.getLogger()
            .addBuildLogEntry("KILL API: " + this.kill_api);
          this.exec_obj.setCalledAPI(this.kill_api);
        
          this.exec_obj.setReqBody(payload.getPayload());
          this.exec_obj.setRespBody(response.getResponseBody());
        }

        return ret_flag;
      }
    }
  }
}
