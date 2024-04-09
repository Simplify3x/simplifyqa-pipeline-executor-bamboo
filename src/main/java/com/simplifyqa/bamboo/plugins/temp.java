package com.simplifyqa.bamboo.plugins;

import com.simplifyqa.bamboo.plugins.impl.HttpResponse;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class temp {

  private static final int CONNECTION_TIMEOUT = 300000; // 5 minutes

  public static HttpResponse sendGetRequest(String url) throws Exception {
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

  public static HttpResponse sendPostRequest(String url, String body)
    throws Exception {
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

  public static Properties readPropertiesFile(String filePath) {
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(filePath)) {
      properties.load(fis);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return properties;
  }

  public static void main(String[] args) throws Exception {
    // Properties prop = temp.readPropertiesFile(
    //   "src/main/resources/properties/plugin.properties"
    // );
    // prop.forEach((key, value) -> System.out.println(key + " : " + value));
    // System.out.println(prop.getProperty("com.simplifyqa.fields.token.nameKey"));
    String url = "https://qa.simplifyqa.app/jenkinsSuiteExecution";
    String payload =
      "{\"token\": \"U2FsdGVkX1/SZh7Ibhl4dNIWTFOED77s8TFv2JUR/VanSl3m6bkKGLZfz/GO9oyFN+vTYMyl/bJ7J3c5p06gvw==\"}";
    HttpResponse resp = temp.sendPostRequest(url, payload);
    System.out.println(resp.getResponseBody());
    System.out.println(resp.getResponseCode());
  }
}
