package io.github.makki93.consorsbank.mcp.http;

import io.github.makki93.consorsbank.mcp.model.common.ApiErrorResponse;
import lombok.Getter;

@Getter
public class ConsorsbankApiException extends RuntimeException {
  private final int statusCode;
  private final ApiErrorResponse errorResponse;
  private final String responseBody;

  public ConsorsbankApiException(int statusCode, ApiErrorResponse errorResponse, String responseBody) {
    super(buildMessage(statusCode, errorResponse, responseBody));
    this.statusCode = statusCode;
    this.errorResponse = errorResponse;
    this.responseBody = responseBody;
  }

  private static String buildMessage(int statusCode, ApiErrorResponse errorResponse, String responseBody) {
    if (errorResponse == null) {
      return "Consorsbank API error %s: %s".formatted(statusCode, responseBody);
    }

    String details = errorResponse.details() == null || errorResponse.details().isEmpty()
        ? ""
        : " details=" + errorResponse.details();

    return "Consorsbank API error %s %s: %s%s".formatted(
        errorResponse.http(),
        errorResponse.code(),
        errorResponse.developerText(),
        details);
  }
}
