package io.github.makki93.consorsbank.mcp.util;

import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import java.util.List;
import java.util.Map;

public final class Schemas {
  private Schemas() {
  }

  public static JsonSchema object(Map<String, Object> properties, List<String> required) {
    return new JsonSchema("object", properties, required, false, null, null);
  }

  public static Map<String, Object> string(String description) {
    return Map.of("type", "string", "description", description);
  }

  public static Map<String, Object> integer(String description) {
    return Map.of("type", "integer", "description", description);
  }

  public static Map<String, Object> bool(String description) {
    return Map.of("type", "boolean", "description", description);
  }

  public static Map<String, Object> array(String description, Map<String, Object> items) {
    return Map.of("type", "array", "description", description, "items", items);
  }
}
