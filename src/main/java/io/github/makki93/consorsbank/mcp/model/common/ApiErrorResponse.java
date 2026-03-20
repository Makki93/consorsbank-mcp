package io.github.makki93.consorsbank.mcp.model.common;

import java.util.List;

public record ApiErrorResponse(
    String http,
    String code,
    String developerText,
    List<ApiErrorDetail> details) {

  public record ApiErrorDetail(String key, String text) {
  }
}
