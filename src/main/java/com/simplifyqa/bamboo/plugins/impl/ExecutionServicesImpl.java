package com.simplifyqa.bamboo.plugins.impl;

import com.simplifyqa.bamboo.plugins.api.Execution;
import com.simplifyqa.bamboo.plugins.api.ExecutionServices;
import com.simplifyqa.bamboo.plugins.api.payloads.GenericPayload;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

  // @Override
  // public HttpResponse makeHttpPostRequest(String url, String payload) {
  //   int timeout = 5 * 60;
  //   RequestConfig config = RequestConfig
  //     .custom()
  //     .setConnectTimeout(timeout * 1000)
  //     .setConnectionRequestTimeout(timeout * 1000)
  //     .setSocketTimeout(timeout * 1000)
  //     .build();

  //   BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();

  //   CloseableHttpClient client = HttpClients
  //     .custom()
  //     .setConnectionManager(connectionManager)
  //     .build();

  //   HttpPost request = new HttpPost(url);
  //   StringEntity entity;
  //   try {
  //     entity = new StringEntity(payload);

  //     entity.setContentType("application/json");
  //     request.setEntity(entity);
  //     request.setHeader("Content-Type", "application/json");
  //     request.setHeader("Authorization", this.exec_obj.getAuthKey());

  //     return client.execute(request);
  //   } catch (UnsupportedEncodingException e) {
  //     e.printStackTrace();
  //     return null;
  //   } catch (ClientProtocolException e) {
  //     e.printStackTrace();
  //     return null;
  //   } catch (IOException e) {
  //     e.printStackTrace();
  //     return null;
  //   }
  // }

  // @Override
  // public HttpResponse makeHttpGetRequest(String url)
  //   throws ClientProtocolException, IOException {
  //   int timeout = 5;
  //   RequestConfig config = RequestConfig
  //     .custom()
  //     .setConnectTimeout(timeout * 1000)
  //     .setConnectionRequestTimeout(timeout * 1000)
  //     .setSocketTimeout(timeout * 1000)
  //     .build();

  //   CloseableHttpClient client = HttpClientBuilder
  //     .create()
  //     .setDefaultRequestConfig(config)
  //     .build();

  //   HttpGet request = new HttpGet(url);

  //   return client.execute(request);
  // }
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
  public boolean startExec() throws IOException {
    if (this.exec_obj == null) return false; else {
      GenericPayload payload = GenericPayload.createTriggerPayload(
        this.exec_obj
      );
      HttpResponse response =
        this.makeHttpPostRequest(this.build_api, payload.getPayload());
      if (response == null) return false; else {
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
            } catch (ParseException PE) {
              PE.printStackTrace();
              return false;
            }

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                String.format(
                  ExecutionServices.getTimestamp(),
                  "EXECUTION STATUS: Status code " +
                  response.getResponseCode() +
                  ", Execution triggered."
                )
              );

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServices.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );

              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServices.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            return true;
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
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );

              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            return false;
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

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );

              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            return false;
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

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            return false;
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

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }
            return false;
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
                "REASON OF FAILURE: Something is critically broken on SQA Servers."
              );

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            return false;
        }
      }
    }
  }

  @Override
  public String checkExecStatus() throws InterruptedException, IOException {
    if (this.exec_obj == null) return "FAILED"; else {
      int failsafe_counter = 60;
      GenericPayload payload = GenericPayload.createStatusPayload(
        this.exec_obj
      );

      HttpResponse response = null;
      try {
        do {
          response =
            this.makeHttpPostRequest(this.status_api, payload.getPayload());

          try {
            JSONObject jsonResponse = (JSONObject) new JSONParser()
              .parse(response.getResponseBody());
            if (Boolean.valueOf(jsonResponse.get("success").toString())) {
              break; // Break the loop if success is true
            }
          } catch (ParseException e) {
            // Handle parsing exception
            e.printStackTrace();
          }

          Thread.sleep(5000);
          failsafe_counter--;
        } while (failsafe_counter > 0);
      } catch (IOException e) {
        e.printStackTrace();
      }

      if (response == null) return "FAILED"; else {
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
                  String.format(
                    ExecutionServices.getTimestamp(),
                    "EXECUTION STATUS: Status code " +
                    response.getResponseCode() +
                    ", Execution Status fetched successfully."
                  )
                );

              if (this.exec_obj.getVerbose()) {
                this.exec_obj.getLogger()
                  .addBuildLogEntry(
                    ExecutionServices.getTimestamp() +
                    "REQUEST BODY: " +
                    payload.getPayload()
                  );

                this.exec_obj.getLogger()
                  .addBuildLogEntry(
                    ExecutionServices.getTimestamp() +
                    "RESPONSE BODY: " +
                    response.getResponseBody()
                  );
              }

              PE.printStackTrace();
              return "FAILED";
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

            this.exec_obj.setExecStatus((String) dataObj.get("execution"));

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                String.format(
                  ExecutionServices.getTimestamp(),
                  "EXECUTION STATUS: Status code " +
                  response.getResponseCode() +
                  ", Execution Status fetched successfully."
                )
              );

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServices.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );

              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServices.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            return this.exec_obj.getExecStatus();
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

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            this.exec_obj.setExecStatus("FAILED");

            return "FAILED";
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

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            this.exec_obj.setExecStatus("FAILED");

            return "FAILED";
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

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            this.exec_obj.setExecStatus("FAILED");

            return "FAILED";
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

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            this.exec_obj.setExecStatus("FAILED");

            return "FAILED";
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

            if (this.exec_obj.getVerbose()) {
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "REQUEST BODY: " +
                  payload.getPayload()
                );
              this.exec_obj.getLogger()
                .addBuildLogEntry(
                  ExecutionServicesImpl.getTimestamp() +
                  "RESPONSE BODY: " +
                  response.getResponseBody()
                );
            }

            this.exec_obj.setExecStatus("FAILED");

            return "FAILED";
        }
      }
    }
  }

  @Override
  public boolean killExec() throws IOException {
    if (this.exec_obj == null) return false; else {
      GenericPayload payload = GenericPayload.createKillPayload(this.exec_obj);
      HttpResponse response =
        this.makeHttpPostRequest(this.kill_api, payload.getPayload());
      if (response == null) return false; else {
        switch (response.getResponseCode()) {
          case 200:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Execution Killed Successfully."
              );

            return true;
          case 400:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execiution."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: Logout and login again, Invalid Authorization token for the specified env: " +
                this.exec_obj.getApp_url()
              );

            this.exec_obj.setExecStatus("FAILED");

            return false;
          case 403:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execiution."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: Logout and login again, Invalid Authorization token for the specified env: " +
                this.exec_obj.getApp_url()
              );

            this.exec_obj.setExecStatus("FAILED");

            return false;
          case 500:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execiution."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: The Pipeline Token is invalid for the specified env: " +
                this.exec_obj.getApp_url()
              );

            this.exec_obj.setExecStatus("FAILED");

            return false;
          case 504:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execiution."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: The server gateway timed-out for the specified env: " +
                this.exec_obj.getApp_url()
              );

            this.exec_obj.setExecStatus("FAILED");

            return false;
          default:
            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "EXECUTION STATUS: Status code " +
                response.getResponseCode() +
                ", Failed to kill execiution."
              );

            this.exec_obj.getLogger()
              .addBuildLogEntry(
                ExecutionServicesImpl.getTimestamp() +
                "REASON OF FAILURE: An unclassified error has occurred." +
                this.exec_obj.getApp_url()
              );

            this.exec_obj.setExecStatus("FAILED");

            return false;
        }
      }
    }
  }
}
