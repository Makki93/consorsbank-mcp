package io.github.makki93.consorsbank.mcp.tools;

import io.github.makki93.consorsbank.mcp.config.AppConfig;
import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.util.List;
import java.util.Map;

public final class ServerTools {
  private ServerTools() {
  }

  public static Tool serverInfoTool() {
    return Tool.builder()
        .name("get_server_info")
        .description("Show current Consorsbank MCP runtime configuration and target environment.")
        .inputSchema(emptyObjectSchema())
        .build();
  }

  public static Tool pingConsorsbankTool() {
    return Tool.builder()
        .name("ping_consorsbank_api")
        .description("Perform a simple unauthenticated request build check and report the target base URL.")
        .inputSchema(emptyObjectSchema())
        .build();
  }

  public static CallToolResult serverInfo(AppConfig config) {
    String payload = """
        targetBaseUrl: %s
        sandboxEnabled: %s
        accessTokenConfigured: %s
        requestTimeoutSeconds: %s
        pollIntervalMillis: %s
        maxPollAttempts: %s
        """.formatted(
        config.baseUrl(),
        config.sandbox(),
        config.hasAccessToken(),
        config.requestTimeout().toSeconds(),
        config.pollInterval().toMillis(),
        config.maxPollAttempts());

    return CallToolResult.builder()
        .addTextContent(payload)
        .isError(false)
        .build();
  }

  public static CallToolResult pingConsorsbank(ConsorsbankHttpClient httpClient) {
    String payload = """
        HTTP client initialized successfully.
        Target base URL: %s
        Example positions endpoint: %s
        """.formatted(
        httpClient.resolve("/"),
        httpClient.resolve("/v1/securities-accounts"));

    return CallToolResult.builder()
        .addTextContent(payload)
        .isError(false)
        .build();
  }

  private static JsonSchema emptyObjectSchema() {
    return new JsonSchema("object", Map.of(), List.of(), false, null, null);
  }
}
