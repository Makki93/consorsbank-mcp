package io.github.makki93.consorsbank.mcp.util;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.Map;

public final class ToolResults {
  private ToolResults() {
  }

  public static CallToolResult json(Object value) {
    return CallToolResult.builder()
        .structuredContent(value)
        .addTextContent(JsonSupport.toJson(value))
        .isError(false)
        .build();
  }

  public static CallToolResult message(String text) {
    return CallToolResult.builder()
        .addTextContent(text)
        .isError(false)
        .build();
  }

  public static CallToolResult error(String text) {
    return CallToolResult.builder()
        .addTextContent(text)
        .isError(true)
        .meta(Map.of("error", true))
        .build();
  }
}
