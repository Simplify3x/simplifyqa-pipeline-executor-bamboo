package io.jenkins.plugins.simplifyqa.Services;

import io.jenkins.plugins.simplifyqa.ExecutionImpl;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ExecutionServices {

    private static final int REQUEST_TIMEOUT_IN_SECS = 300; // Added 5 minutes of Request Timeout for cloud
    // containers

    private ExecutionImpl exec_obj = null;
    private static final String bannerPath = "src\\main\\resources\\io\\jenkins\\plugins\\SQA\\banner.txt";

    // Getters
    public static String getTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        Date date = new Date();
        return "\n[" + formatter.format(date) + " Hrs] ";
    }

    public static String getBanner() throws IOException {
        return String.join("\n", Files.readAllLines(Paths.get(ExecutionServices.bannerPath)));
    }

    public ExecutionImpl getExecObj() {
        return this.exec_obj;
    }

    // Setters
    public void setExecObj(ExecutionImpl exec_obj) {
        this.exec_obj = exec_obj;
        return;
    }

    // Services

    public static HttpResponse<String> getResponse(String url, String method, String payload)
            throws URISyntaxException {
        HttpRequest request = null;
        HttpResponse<String> response = null;

        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_NONE))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URI(url))
                .timeout(Duration.of(ExecutionServices.REQUEST_TIMEOUT_IN_SECS, ChronoUnit.SECONDS));
        builder.setHeader("Content-Type", "application/json");
        builder.setHeader("Authorization", ExecutionImpl.getAuthKey());

        try {
            if (method.equalsIgnoreCase("GET"))
                request = builder.GET().version(HttpClient.Version.HTTP_2).build();

            if (method.equalsIgnoreCase("POST"))
                request = builder.POST(HttpRequest.BodyPublishers.ofString(payload))
                        .version(HttpClient.Version.HTTP_2)
                        .build();

            response = client.send(request, BodyHandlers.ofString());

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return response;
        }
    }

    public void printLog(String toPrint) {
        if (toPrint.trim().length() == 0) return;
        this.exec_obj.addLogs(toPrint);
        this.exec_obj.getListener().getLogger().print(toPrint);
    }

    public boolean startExec() {
        boolean ret_flag = false;
        HttpResponse<String> response = null;
        int failsafe_counter = 60; // The failsafe counter is used to ensure the app to waits until the
        // execution is properly started or response is properly determined
        TriggerPayload payload = new TriggerPayload(this.exec_obj.getExec_token());
        try {
            response = ExecutionServices.getResponse(exec_obj.getBuildApi(), "POST", payload.getPayload());

            while (!(Boolean.valueOf(((JSONObject) new JSONParser().parse(response.body()))
                            .get("success")
                            .toString()))
                    && (failsafe_counter > 0)) {
                response = ExecutionServices.getResponse(exec_obj.getBuildApi(), "POST", payload.getPayload());
                Thread.sleep(5000);
                failsafe_counter--;
            }

            switch (response.statusCode()) {
                case 200:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution triggered.");

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }

                    this.exec_obj.setExecId((long)
                            ((JSONObject) new JSONParser().parse(response.body().toString())).get("executionId"));

                    this.exec_obj.setCustomerId(Integer.parseInt((((JSONObject) new JSONParser()
                                            .parse(response.body().toString()))
                                    .get("customerId"))
                            .toString()));

                    this.exec_obj.setProjectId(Integer.parseInt((((JSONObject) new JSONParser()
                                            .parse(response.body().toString()))
                                    .get("projectId"))
                            .toString()));

                    ExecutionImpl.setAuthKey((String)
                            ((JSONObject) new JSONParser().parse(response.body().toString())).get("authKey"));

                    this.printLog(ExecutionServices.getTimestamp()
                            + "EXECUTION STATUS: INITIALIZING TESTCASES in the triggered suite");

                    ret_flag = true;
                    break;
                case 400:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution did not get triggered.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: Invalid Execution token for the specified env: "
                            + this.exec_obj.getApp_url());

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    break;
                case 403:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution did not get triggered.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: Invalid Execution token for the specified env: "
                            + this.exec_obj.getApp_url());

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    break;
                case 500:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution did not get triggered.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: The cloud server or the local machine is unavailable for the specified env: "
                            + this.exec_obj.getApp_url());

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    break;
                case 504:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution did not get triggered.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: The server gateway timed-out for the specified env: "
                            + this.exec_obj.getApp_url());

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    break;
                default:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution did not get triggered.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: Something is critically broken on SQA Servers.");

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    break;
            }

            return ret_flag;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            this.printLog(
                    ExecutionServices.getTimestamp() + "EXECUTION STATUS: No response received. Is the server down?");

            e.printStackTrace();
            return ret_flag;
        }
    }

    public String checkExecStatus() {
        HttpResponse<String> response = null;
        StringBuilder toPrint = new StringBuilder();
        int failsafe_counter = 60; // The failsafe counter is used to ensure the app to waits until the
        // execution
        // is properly started or response is properly determined

        StatusPayload payload = new StatusPayload(
                this.exec_obj.getExecId(), this.exec_obj.getCustomerId(), this.exec_obj.getProjectId());
        try {
            response = ExecutionServices.getResponse(exec_obj.getBuildApi(), "POST", payload.getPayload());

            while (!(Boolean.valueOf(((JSONObject) new JSONParser().parse(response.body()))
                            .get("success")
                            .toString()))
                    && (failsafe_counter > 0)) {
                response = ExecutionServices.getResponse(exec_obj.getStatusApi(), "POST", payload.getPayload());
                Thread.sleep(5000);
                failsafe_counter--;
            }

            switch (response.statusCode()) {
                case 200:
                    // this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status
                    // code "
                    // + response.statusCode() + ", Execution Status fetched successfully.");

                    JSONObject dataObj = (JSONObject) new JSONParser()
                            .parse(((JSONObject) new JSONParser()
                                            .parse(((JSONObject) new JSONParser()
                                                            .parse(response.body()
                                                                    .toString()))
                                                    .get("data")
                                                    .toString()))
                                    .get("data")
                                    .toString());

                    this.exec_obj.setTcsFailed(0);
                    for (Object item : (JSONArray) dataObj.get("result"))
                        if (((JSONObject) item).get("result").toString().equalsIgnoreCase("FAILED"))
                            this.exec_obj.setTcsFailed(this.exec_obj.getTcsFailed() + 1);

                    this.exec_obj.setExecutedTcs(0);
                    for (Object item : (JSONArray) dataObj.get("result"))
                        if ((((JSONObject) item).get("result").toString().equalsIgnoreCase("PASSED"))
                                || (((JSONObject) item).get("result").toString().equalsIgnoreCase("FAILED")))
                            this.exec_obj.setExecutedTcs(this.exec_obj.getExecutedTcs() + 1);

                    this.exec_obj.setResults(((JSONArray) dataObj.get("result")));
                    this.exec_obj.setTotalTcs(
                            Integer.parseInt(dataObj.get("totalTestcases").toString()));
                    this.exec_obj.setSuiteId(
                            Integer.parseInt(dataObj.get("suiteId").toString()));
                    this.exec_obj.setReportUrl((String) dataObj.get("reporturl"));
                    this.exec_obj.setExecStatus((String) dataObj.get("execution"));
                    this.exec_obj.setUserId(
                            Integer.parseInt(dataObj.get("userId").toString()));
                    this.exec_obj.setUserName((String) dataObj.get("username"));
                    this.exec_obj.setFailPercent();
                    this.exec_obj.setExecPercent();

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    break;
                case 400:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution Status could not be fetched.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: Logout and login again, Invalid Execution token for the specified env: "
                            + this.exec_obj.getApp_url());

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    this.exec_obj.setExecStatus("FAILED");
                    break;
                case 403:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution Status could not be fetched.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: Logout and login again, Invalid Authorization token for the specified env: "
                            + this.exec_obj.getApp_url());

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    this.exec_obj.setExecStatus("FAILED");
                    break;
                case 500:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution Status could not be fetched.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: The cloud server or the local machine is unavailable for the specified env: "
                            + this.exec_obj.getApp_url());

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    this.exec_obj.setExecStatus("FAILED");
                    break;
                case 504:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution Status could not be fetched.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: The server gateway timed-out for the specified env: "
                            + this.exec_obj.getApp_url());

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    this.exec_obj.setExecStatus("FAILED");
                    break;
                default:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution Status could not be fetched.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: Something is critically broken on SQA Servers.");

                    if (exec_obj.getVerbose()) {
                        this.exec_obj.setReqBody(
                                ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                        this.exec_obj.setRespBody(
                                ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
                    }
                    this.exec_obj.setExecStatus("FAILED");
                    break;
            }

            this.exec_obj.addLogs(toPrint.toString());
            this.exec_obj.getListener().getLogger().println(toPrint.toString());
            toPrint.delete(0, toPrint.length());

            return this.exec_obj.getExecStatus();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            this.printLog(
                    ExecutionServices.getTimestamp() + "EXECUTION STATUS: No response received. Is the server down?");
            this.exec_obj.addLogs(toPrint.toString());
            this.exec_obj.getListener().getLogger().println(toPrint.toString());
            toPrint.delete(0, toPrint.length());

            e.printStackTrace();
            return "FAILED";
        }
    }

    public boolean killExec() {
        boolean ret_flag = false;
        HttpResponse<String> response = null;

        KillPayload payload = new KillPayload(
                this.exec_obj.getCustomerId(),
                this.exec_obj.getExecId(),
                this.exec_obj.getUserId(),
                this.exec_obj.getUserName());
        try {
            response = ExecutionServices.getResponse(exec_obj.getKillApi(), "POST", payload.getPayload());
            switch (response.statusCode()) {
                case 200:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Execution Killed Successfully.");
                    ret_flag = true;
                    break;
                case 400:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Failed to kill execiution.");
                    this.printLog(ExecutionServices.getTimestamp() + this.exec_obj.getApp_url());
                    this.exec_obj.setExecStatus("FAILED");
                    break;
                case 403:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Failed to kill execiution.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: Logout and login again, Invalid Authorization token for the specified env: "
                            + this.exec_obj.getApp_url());
                    this.exec_obj.setExecStatus("FAILED");
                    break;
                case 500:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Failed to kill execiution.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: The Pipeline Token is invalid for the specified env: "
                            + this.exec_obj.getApp_url());
                    this.exec_obj.setExecStatus("FAILED");
                    break;
                case 504:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Failed to kill execiution.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: The server gateway timed-out for the specified env: "
                            + this.exec_obj.getApp_url());
                    this.exec_obj.setExecStatus("FAILED");
                    break;
                default:
                    this.printLog(ExecutionServices.getTimestamp() + "EXECUTION STATUS: Status code "
                            + response.statusCode() + ", Failed to kill execiution.");
                    this.printLog(ExecutionServices.getTimestamp()
                            + "REASON OF FAILURE: Something is critically broken on SQA Servers.");
                    this.exec_obj.setExecStatus("FAILED");
                    break;
            }

            if (exec_obj.getVerbose()) {
                this.exec_obj.setReqBody(ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                this.exec_obj.setRespBody(ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
            }

            return ret_flag;
        } catch (Exception e) {
            this.printLog(
                    ExecutionServices.getTimestamp() + "EXECUTION STATUS: No response received. Is the server down?");
            if (exec_obj.getVerbose()) {
                this.exec_obj.setReqBody(ExecutionServices.getTimestamp() + "REQUEST BODY: " + payload.getPayload());
                this.exec_obj.setRespBody(ExecutionServices.getTimestamp() + "RESPONSE BODY: " + response.body());
            }

            e.printStackTrace();
            return ret_flag;
        }
    }
}
