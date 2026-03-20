package io.github.makki93.consorsbank.mcp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestArgumentsTest {
  @Test
  void parsesOptionalValuesAcrossSupportedShapes() {
    Map<String, Object> arguments = Map.of(
        "count", 3,
        "enabled", "true",
        "tags", List.of("one", "two"),
        "name", "demo");

    assertEquals("demo", RequestArguments.requiredString(arguments, "name"));
    assertEquals(3, RequestArguments.optionalInteger(arguments, "count"));
    assertTrue(RequestArguments.optionalBoolean(arguments, "enabled", false));
    assertIterableEquals(List.of("one", "two"), RequestArguments.optionalStringList(arguments, "tags"));
  }

  @Test
  void fallsBackAndRejectsMissingRequiredValues() {
    Map<String, Object> arguments = Map.of("enabled", "no");

    assertFalse(RequestArguments.optionalBoolean(arguments, "enabled", true));
    assertNull(RequestArguments.optionalString(arguments, "missing"));
    assertThrows(IllegalArgumentException.class, () -> RequestArguments.requiredString(arguments, "missing"));
  }
}
