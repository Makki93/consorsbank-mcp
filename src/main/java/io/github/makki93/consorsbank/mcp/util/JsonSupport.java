package io.github.makki93.consorsbank.mcp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonSupport {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .enable(SerializationFeature.INDENT_OUTPUT);

  private JsonSupport() {
  }

  public static ObjectMapper objectMapper() {
    return OBJECT_MAPPER;
  }

  public static String toJson(Object value) {
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize value to JSON", exception);
    }
  }

  public static <T> T convertValue(Object value, Class<T> targetType) {
    return OBJECT_MAPPER.convertValue(value, targetType);
  }
}
