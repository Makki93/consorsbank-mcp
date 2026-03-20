package io.github.makki93.consorsbank.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.makki93.consorsbank.mcp.config.AppConfig;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ServerToolsTest {
  @Test
  void serverInfoIncludesModeAndTargetWithoutLeakingToken() throws Exception {
    AppConfig config = config(Map.of(
        "CONSORS_TARGET_ENV", "sandbox",
        "CONSORS_MODE", "read-only",
        "CONSORS_ACCESS_TOKEN", "secret-value"));

    String payload = ((TextContent) ServerTools.serverInfo(config).content().getFirst()).text();

    assertTrue(payload.contains("targetEnvironment: sandbox"));
    assertTrue(payload.contains("mode: read-only"));
    assertTrue(payload.contains("accessTokenConfigured: true"));
    assertFalse(payload.contains("secret-value"));
  }

  private static AppConfig config(Map<String, String> env) throws Exception {
    Method fromEnvironment = AppConfig.class.getDeclaredMethod("fromEnvironment", Map.class);
    fromEnvironment.setAccessible(true);
    return (AppConfig) fromEnvironment.invoke(null, env);
  }
}
