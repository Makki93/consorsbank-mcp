package io.github.makki93.consorsbank.mcp.util;

import java.util.List;
import java.util.Map;

public final class RequestArguments {
  private RequestArguments() {
  }

  public static String requiredString(Map<String, Object> arguments, String key) {
    Object value = arguments.get(key);
    if (value == null || value.toString().isBlank()) {
      throw new IllegalArgumentException("Missing required argument: " + key);
    }
    return value.toString();
  }

  public static String optionalString(Map<String, Object> arguments, String key) {
    Object value = arguments.get(key);
    return value == null ? null : value.toString();
  }

  public static Integer optionalInteger(Map<String, Object> arguments, String key) {
    Object value = arguments.get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.parseInt(value.toString());
  }

  public static List<String> optionalStringList(Map<String, Object> arguments, String key) {
    Object value = arguments.get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof List<?> list) {
      return list.stream().map(String::valueOf).toList();
    }
    return List.of(value.toString());
  }

  public static boolean optionalBoolean(Map<String, Object> arguments, String key, boolean defaultValue) {
    Object value = arguments.get(key);
    if (value == null) {
      return defaultValue;
    }
    return switch (value.toString().trim().toLowerCase()) {
      case "true", "1", "yes", "on" -> true;
      case "false", "0", "no", "off" -> false;
      default -> defaultValue;
    };
  }
}
