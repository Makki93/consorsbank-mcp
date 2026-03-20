package io.github.makki93.consorsbank.mcp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
