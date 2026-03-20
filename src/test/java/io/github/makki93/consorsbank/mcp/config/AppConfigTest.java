package io.github.makki93.consorsbank.mcp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AppConfigTest {
  @Test
  void defaultsToProductionConfiguration() {
    AppConfig config = AppConfig.fromEnvironment(Map.of());

    assertFalse(config.sandbox());
    assertEquals("https://api.consorsbank.de/trading", config.baseUrl().toString());
    assertEquals(30, config.requestTimeout().toSeconds());
    assertEquals(1500, config.pollInterval().toMillis());
    assertEquals(10, config.maxPollAttempts());
  }

  @Test
  void supportsSandboxOverride() {
    AppConfig config = AppConfig.fromEnvironment(Map.of(
        "CONSORS_SANDBOX", "true",
        "CONSORS_ACCESS_TOKEN", "token-value"));

    assertTrue(config.sandbox());
    assertEquals("https://api.consorsbank.de/sandbox/trading", config.baseUrl().toString());
    assertTrue(config.hasAccessToken());
  }

  @Test
  void supportsExplicitTargetEnvironmentAndMode() throws Exception {
    AppConfig config = AppConfig.fromEnvironment(Map.of(
        "CONSORS_TARGET_ENV", "sandbox",
        "CONSORS_MODE", "read-only"));

    assertTrue(config.sandbox());
    assertEquals("SANDBOX", invokeString(config, "targetEnvironment"));
    assertEquals("READ_ONLY", invokeString(config, "accessMode"));
  }

  @Test
  void supportsDedicatedBaseUrlOverrides() {
    AppConfig config = AppConfig.fromEnvironment(Map.of(
        "CONSORS_TARGET_ENV", "sandbox",
        "CONSORS_PROD_BASE_URL", "https://prod.example.test/trading",
        "CONSORS_SANDBOX_BASE_URL", "https://sandbox.example.test/trading"));

    assertEquals("https://sandbox.example.test/trading", config.baseUrl().toString());
  }

  @Test
  void resolvesAccessTokenFromFile() throws IOException {
    Path tokenFile = Files.createTempFile("consors-token", ".txt");
    Files.writeString(tokenFile, "file-token\n");

    AppConfig config = AppConfig.fromEnvironment(Map.of(
        "CONSORS_ACCESS_TOKEN_FILE", tokenFile.toString()));

    assertEquals("file-token", config.accessToken());
    assertTrue(config.hasAccessToken());
  }

  @Test
  void resolvesAccessTokenFromCommand() {
    AppConfig config = AppConfig.fromEnvironment(Map.of(
        "CONSORS_ACCESS_TOKEN_COMMAND", "printf command-token"));

    assertEquals("command-token", config.accessToken());
    assertTrue(config.hasAccessToken());
  }

  @Test
  void rejectsMultipleAccessTokenSources() {
    assertThrows(IllegalArgumentException.class, () -> AppConfig.fromEnvironment(Map.of(
        "CONSORS_ACCESS_TOKEN", "env-token",
        "CONSORS_ACCESS_TOKEN_FILE", "/tmp/consors-token")));
  }

  @Test
  void summaryExposesModeAndTargetWithoutLeakingToken() {
    AppConfig config = AppConfig.fromEnvironment(Map.of(
        "CONSORS_TARGET_ENV", "prod",
        "CONSORS_MODE", "read-only",
        "CONSORS_ACCESS_TOKEN", "secret-value"));

    assertTrue(config.summary().contains("mode=read-only"));
    assertTrue(config.summary().contains("targetEnvironment=prod"));
    assertFalse(config.summary().contains("secret-value"));
  }

  private static String invokeString(AppConfig config, String methodName) throws Exception {
    Method method = AppConfig.class.getMethod(methodName);
    Object result = method.invoke(config);
    return result == null ? null : result.toString();
  }
}
