package io.github.makki93.consorsbank.mcp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.makki93.consorsbank.mcp.config.AppConfig;
import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MainToolRegistrationTest {
  @Test
  void readOnlyModeOnlyExposesSafeTools() throws Exception {
    Set<String> toolNames = toolNamesFor(Map.of("CONSORS_MODE", "read-only"));

    assertTrue(toolNames.contains("get_server_info"));
    assertTrue(toolNames.contains("ping_consorsbank_api"));
    assertTrue(toolNames.contains("get_securities_accounts"));
    assertTrue(toolNames.contains("get_positions"));
    assertTrue(toolNames.contains("get_authentication_data"));
    assertTrue(toolNames.contains("get_orders"));
    assertFalse(toolNames.contains("get_quote"));
    assertFalse(toolNames.contains("elevate_session_level"));
    assertFalse(toolNames.contains("activate_session_tan"));
    assertFalse(toolNames.contains("create_order_entry"));
    assertFalse(toolNames.contains("cancel_order"));
  }

  @Test
  void fullModeExposesTradingAndSessionMutationTools() throws Exception {
    Set<String> toolNames = toolNamesFor(Map.of("CONSORS_MODE", "full"));

    assertTrue(toolNames.contains("get_quote"));
    assertTrue(toolNames.contains("elevate_session_level"));
    assertTrue(toolNames.contains("activate_session_tan"));
    assertTrue(toolNames.contains("create_order_entry"));
    assertTrue(toolNames.contains("cancel_order"));
  }

  @SuppressWarnings("unchecked")
  private static Set<String> toolNamesFor(Map<String, String> env) throws Exception {
    Method fromEnvironment = AppConfig.class.getDeclaredMethod("fromEnvironment", Map.class);
    fromEnvironment.setAccessible(true);
    AppConfig config = (AppConfig) fromEnvironment.invoke(null, env);
    ConsorsbankHttpClient httpClient = new ConsorsbankHttpClient(config);
    Method method = Main.class.getDeclaredMethod("toolSpecifications", AppConfig.class, ConsorsbankHttpClient.class);
    method.setAccessible(true);
    List<SyncToolSpecification> toolSpecifications =
        (List<SyncToolSpecification>) method.invoke(null, config, httpClient);
    return toolSpecifications.stream()
        .map(specification -> specification.tool().name())
        .collect(Collectors.toSet());
  }
}
