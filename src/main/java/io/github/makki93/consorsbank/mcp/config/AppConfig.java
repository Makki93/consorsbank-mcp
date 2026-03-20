package io.github.makki93.consorsbank.mcp.config;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public record AppConfig(
    boolean sandbox,
    URI baseUrl,
    String clientId,
    String clientSecret,
    String redirectUri,
    String accessToken,
    String refreshToken,
    Duration requestTimeout,
    Duration pollInterval,
    int maxPollAttempts) {

  private static final URI PROD_BASE_URL = URI.create("https://api.consorsbank.de/trading");
  private static final URI SANDBOX_BASE_URL = URI.create("https://api.consorsbank.de/sandbox/trading");

  public static AppConfig fromEnvironment() {
    return fromEnvironment(System.getenv());
  }

  static AppConfig fromEnvironment(Map<String, String> env) {
    boolean sandbox = parseBoolean(env.getOrDefault("CONSORS_SANDBOX", "false"));
    URI defaultBaseUrl = sandbox ? SANDBOX_BASE_URL : PROD_BASE_URL;
    URI baseUrl = URI.create(env.getOrDefault("CONSORS_BASE_URL", defaultBaseUrl.toString()));

    return new AppConfig(
        sandbox,
        baseUrl,
        env.getOrDefault("CONSORS_CLIENT_ID", ""),
        env.getOrDefault("CONSORS_CLIENT_SECRET", ""),
        env.getOrDefault("CONSORS_REDIRECT_URI", ""),
        env.getOrDefault("CONSORS_ACCESS_TOKEN", ""),
        env.getOrDefault("CONSORS_REFRESH_TOKEN", ""),
        Duration.ofSeconds(parseInt(env.getOrDefault("CONSORS_REQUEST_TIMEOUT_SECONDS", "30"))),
        Duration.ofMillis(parseLong(env.getOrDefault("CONSORS_POLL_INTERVAL_MILLIS", "1500"))),
        parseInt(env.getOrDefault("CONSORS_MAX_POLL_ATTEMPTS", "10")));
  }

  public String summary() {
    return "baseUrl=%s, sandbox=%s, timeout=%ss, pollInterval=%sms, maxPollAttempts=%s".formatted(
        baseUrl,
        sandbox,
        requestTimeout.toSeconds(),
        pollInterval.toMillis(),
        maxPollAttempts);
  }

  public boolean hasAccessToken() {
    return !accessToken.isBlank();
  }

  private static boolean parseBoolean(String value) {
    return switch (value.trim().toLowerCase()) {
      case "1", "true", "yes", "on" -> true;
      default -> false;
    };
  }

  private static int parseInt(String value) {
    return Integer.parseInt(value.trim());
  }

  private static long parseLong(String value) {
    return Long.parseLong(value.trim());
  }
}
