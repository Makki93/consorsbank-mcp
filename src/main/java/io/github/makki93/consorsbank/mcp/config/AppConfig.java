package io.github.makki93.consorsbank.mcp.config;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record AppConfig(
    TargetEnvironment targetEnvironment,
    AccessMode accessMode,
    boolean sandbox,
    URI prodBaseUrl,
    URI sandboxBaseUrl,
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
    TargetEnvironment targetEnvironment = parseTargetEnvironment(env);
    AccessMode accessMode = parseAccessMode(env.getOrDefault("CONSORS_MODE", "full"));
    boolean sandbox = targetEnvironment == TargetEnvironment.SANDBOX;
    URI prodBaseUrl = URI.create(env.getOrDefault("CONSORS_PROD_BASE_URL", PROD_BASE_URL.toString()));
    URI sandboxBaseUrl = URI.create(env.getOrDefault("CONSORS_SANDBOX_BASE_URL", SANDBOX_BASE_URL.toString()));
    URI defaultBaseUrl = sandbox ? sandboxBaseUrl : prodBaseUrl;
    URI baseUrl = URI.create(env.getOrDefault("CONSORS_BASE_URL", defaultBaseUrl.toString()));
    String accessToken = resolveAccessToken(env);

    return new AppConfig(
        targetEnvironment,
        accessMode,
        sandbox,
        prodBaseUrl,
        sandboxBaseUrl,
        baseUrl,
        env.getOrDefault("CONSORS_CLIENT_ID", ""),
        env.getOrDefault("CONSORS_CLIENT_SECRET", ""),
        env.getOrDefault("CONSORS_REDIRECT_URI", ""),
        accessToken,
        env.getOrDefault("CONSORS_REFRESH_TOKEN", ""),
        Duration.ofSeconds(parseInt(env.getOrDefault("CONSORS_REQUEST_TIMEOUT_SECONDS", "30"))),
        Duration.ofMillis(parseLong(env.getOrDefault("CONSORS_POLL_INTERVAL_MILLIS", "1500"))),
        parseInt(env.getOrDefault("CONSORS_MAX_POLL_ATTEMPTS", "10")));
  }

  public String summary() {
    return "targetEnvironment=%s, mode=%s, baseUrl=%s, sandbox=%s, accessTokenConfigured=%s, timeout=%ss, pollInterval=%sms, maxPollAttempts=%s".formatted(
        targetEnvironment.configValue(),
        accessMode.configValue(),
        baseUrl,
        sandbox,
        hasAccessToken(),
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

  private static TargetEnvironment parseTargetEnvironment(Map<String, String> env) {
    String configured = env.get("CONSORS_TARGET_ENV");
    if (configured != null && !configured.isBlank()) {
      return switch (configured.trim().toLowerCase()) {
        case "prod", "production" -> TargetEnvironment.PROD;
        case "sandbox" -> TargetEnvironment.SANDBOX;
        default -> throw new IllegalArgumentException("Unsupported CONSORS_TARGET_ENV: " + configured);
      };
    }

    return parseBoolean(env.getOrDefault("CONSORS_SANDBOX", "false"))
        ? TargetEnvironment.SANDBOX
        : TargetEnvironment.PROD;
  }

  private static AccessMode parseAccessMode(String value) {
    return switch (value.trim().toLowerCase()) {
      case "read-only", "readonly", "read_only" -> AccessMode.READ_ONLY;
      case "full" -> AccessMode.FULL;
      default -> throw new IllegalArgumentException("Unsupported CONSORS_MODE: " + value);
    };
  }

  private static String resolveAccessToken(Map<String, String> env) {
    String directToken = env.getOrDefault("CONSORS_ACCESS_TOKEN", "").trim();
    String tokenFile = env.getOrDefault("CONSORS_ACCESS_TOKEN_FILE", "").trim();
    String tokenCommand = env.getOrDefault("CONSORS_ACCESS_TOKEN_COMMAND", "").trim();

    List<String> configuredSources = new ArrayList<>();
    if (!directToken.isBlank()) {
      configuredSources.add("CONSORS_ACCESS_TOKEN");
    }
    if (!tokenFile.isBlank()) {
      configuredSources.add("CONSORS_ACCESS_TOKEN_FILE");
    }
    if (!tokenCommand.isBlank()) {
      configuredSources.add("CONSORS_ACCESS_TOKEN_COMMAND");
    }

    if (configuredSources.size() > 1) {
      throw new IllegalArgumentException(
          "Configure only one access token source: " + String.join(", ", configuredSources));
    }

    if (!directToken.isBlank()) {
      return directToken;
    }
    if (!tokenFile.isBlank()) {
      return SecretValueResolver.fromFile(tokenFile);
    }
    if (!tokenCommand.isBlank()) {
      return SecretValueResolver.fromCommand(tokenCommand);
    }
    return "";
  }

  private static int parseInt(String value) {
    return Integer.parseInt(value.trim());
  }

  private static long parseLong(String value) {
    return Long.parseLong(value.trim());
  }

  public enum TargetEnvironment {
    PROD,
    SANDBOX;

    public String configValue() {
      return name().toLowerCase();
    }
  }

  public enum AccessMode {
    FULL,
    READ_ONLY;

    public String configValue() {
      return this == READ_ONLY ? "read-only" : "full";
    }
  }
}
