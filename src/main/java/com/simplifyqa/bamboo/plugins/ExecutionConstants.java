package com.simplifyqa.bamboo.plugins;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Manifest;
import org.apache.commons.lang3.StringUtils;

public class ExecutionConstants {

  static final Set<String> COMPLETE_STATUSES = ImmutableSet.of(
    "succeeded",
    "failed",
    "cancelled",
    "completed",
    "terminated"
  );

  static final long EXECUTION_STATUS_POLLING_INTERNAL_MILLISECONDS = 10000;

  private static final String PLUGIN_VERSION = getPluginVersion();
  private static final String PLUGIN_VERSION_UNKNOWN = "unknown";
  static final String PLUGIN_USER_AGENT = String.format(
    "simplifyqa-pipeline-executor/%s (JVM: %s, Bamboo: %s)",
    PLUGIN_VERSION,
    System.getProperty("java.version"),
    getBambooVersion()
  );

  public static final String PROPERTIES_PATH =
    "src/main/resources/properties/plugin.properties";
  public static final String SQA_BASE_URL = "https://simplifyqa.app";

  static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(10);
  static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(30);
  static final Duration CONNECTION_SECONDS_TO_LIVE = Duration.ofSeconds(30);
  static final int RETRY_HANDLER_MAX_RETRIES = 5;
  static final Duration RETRY_HANDLER_RETRY_INTERVAL = Duration.ofMillis(6000);

  public static final String EXEC_TOKEN_FIELD =
    "com.simplifyqa.fields.token.nameKey";
  public static final String EXEC_TOKEN_LABEL_PROPERTY =
    "com.simplifyqa.fields.token.labelKey";

  public static final String APP_URL_FIELD =
    "com.simplifyqa.fields.url.nameKey";
  public static final String APP_URL_LABEL_PROPERTY =
    "com.simplifyqa.fields.url.labelKey";

  public static final String THRESHOLD_FIELD =
    "com.simplifyqa.fields.threshold.nameKey";
  public static final String THRESHOLD_LABEL_PROPERTY =
    "com.simplifyqa.fields.threshold.labelKey";

  public static final String VERBOSE_FIELD =
    "com.simplifyqa.fields.verbose.nameKey";
  public static final String VERBOSE_LABEL_PROPERTY =
    "com.simplifyqa.fields.verbose.labelKey";

  public static final String ADVANCED_CHECK_FIELD =
    "com.simplifyqa.fields.advancedCheck";

  public static final String ERR_INVALID_TOKEN =
    "The Execution Token is invalid";
  public static final String ERR_EMPTY_TOKEN =
    "The Execution Token is a required field";
  public static final String WARN_INVALID_APP_URL =
    "The App URL is invalid. (Resolve to default app url: https://simplifyqa.app)";
  public static final String WARN_UNREACHABLE_APP_URL =
    "The App URL is unreachable. (Resolve to default app url: https://simplifyqa.app)";
  public static final String ERR_EMPTY_APP_URL =
    "The App URL is a required field. (Resolve to default app url: https://simplifyqa.app)";
  public static final String WARN_INVALID_THRESHOLD =
    "The Threshold value is invalid. Threshold Range: 1.00% to 100.00%. (Resolve to default threshold: 100)";

  static final String SIMPLIFYQA_LOG_OUTPUT_PREFIX = "[simplifyqa]";
  static final String SIMPLIFYQA_JUNIT_REPORT_XML = "report.xml";

  private static final String PLUGIN_SYMBOLIC_NAME =
    "com.simplifyqa.bamboo.plugin.PipelineExecutor";

  private static String getPluginVersion() {
    try {
      final Enumeration<URL> resources =
        ExecutionConstants.class.getClassLoader()
          .getResources("META-INF/MANIFEST.MF");
      while (resources.hasMoreElements()) {
        final Manifest manifest = new Manifest(
          resources.nextElement().openStream()
        );

        String title = manifest
          .getMainAttributes()
          .getValue("Bundle-SymbolicName");
        if (PLUGIN_SYMBOLIC_NAME.equalsIgnoreCase(title)) {
          final String version = manifest
            .getMainAttributes()
            .getValue("Bundle-Version");
          return version != null && !version.isEmpty()
            ? version
            : PLUGIN_VERSION_UNKNOWN;
        }
      }
    } catch (IOException ignored) {}
    return PLUGIN_VERSION_UNKNOWN;
  }

  private static String getBambooVersion() {
    return Optional
      .ofNullable(System.getProperty("atlassian.sdk.version"))
      .orElseGet(() -> {
        final String pluginVersion = System.getenv("AMPS_PLUGIN_VERSION"); // it appears this env variable is no
        // longer set.
        return StringUtils.isEmpty(pluginVersion) ? "unknown" : pluginVersion;
      });
  }
}
