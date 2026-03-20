package io.github.makki93.consorsbank.mcp.http;

import io.github.makki93.consorsbank.mcp.config.AppConfig;
import io.github.makki93.consorsbank.mcp.model.common.ApiErrorResponse;
import io.github.makki93.consorsbank.mcp.model.common.CreatedResource;
import io.github.makki93.consorsbank.mcp.util.JsonSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class ConsorsbankHttpClient {
  private static final String ACCEPT_HEADER =
      "application/hal+json, application/hal+json;charset=UTF-8, application/json";

  private final HttpClient httpClient;
  private final AppConfig config;

  public ConsorsbankHttpClient(AppConfig config) {
    this.config = config;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(config.requestTimeout())
        .build();
  }

  public URI resolve(String path) {
    return config.baseUrl().resolve(path);
  }

  public URI resolve(String path, Map<String, ?> queryParameters) {
    StringBuilder builder = new StringBuilder(resolve(path).toString());
    String queryString = toQueryString(queryParameters);
    if (!queryString.isEmpty()) {
      builder.append(builder.indexOf("?") >= 0 ? "&" : "?").append(queryString);
    }
    return URI.create(builder.toString());
  }

  public HttpRequest.Builder request(String path) {
    return request(resolve(path));
  }

  public HttpRequest.Builder request(String path, Map<String, ?> queryParameters) {
    return request(resolve(path, queryParameters));
  }

  private HttpRequest.Builder request(URI uri) {
    HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
        .timeout(config.requestTimeout())
        .header("Accept", ACCEPT_HEADER)
        .header("Content-Type", "application/json");

    if (config.hasAccessToken()) {
      builder.header("Authorization", "Bearer " + config.accessToken());
    }

    return builder;
  }

  public <T> T get(String path, Class<T> responseType) throws IOException, InterruptedException {
    HttpRequest request = request(path).GET().build();
    return send(request, responseType);
  }

  public <T> T get(String path, Map<String, ?> queryParameters, Class<T> responseType)
      throws IOException, InterruptedException {
    HttpRequest request = request(path, queryParameters).GET().build();
    return send(request, responseType);
  }

  public <T> T post(String path, Object body, Class<T> responseType) throws IOException, InterruptedException {
    HttpRequest request = request(path)
        .POST(HttpRequest.BodyPublishers.ofString(JsonSupport.toJson(body)))
        .build();
    return send(request, responseType);
  }

  public <T> T put(String path, Object body, Class<T> responseType) throws IOException, InterruptedException {
    HttpRequest request = request(path)
        .PUT(HttpRequest.BodyPublishers.ofString(JsonSupport.toJson(body)))
        .build();
    return send(request, responseType);
  }

  public <T> T delete(String path, Class<T> responseType) throws IOException, InterruptedException {
    HttpRequest request = request(path).DELETE().build();
    return send(request, responseType);
  }

  public CreatedResource postForReference(String path, Object body) throws IOException, InterruptedException {
    HttpRequest request = request(path)
        .POST(HttpRequest.BodyPublishers.ofString(JsonSupport.toJson(body)))
        .build();
    return extractReference(sendRaw(request));
  }

  private <T> T send(HttpRequest request, Class<T> responseType) throws IOException, InterruptedException {
    HttpResponse<String> response = sendRaw(request);

    if (response.statusCode() >= 400) {
      throw toApiException(response);
    }

    if (responseType == Void.class) {
      return null;
    }

    if (responseType == String.class) {
      return responseType.cast(response.body());
    }

    return JsonSupport.objectMapper().readValue(response.body(), responseType);
  }

  private HttpResponse<String> sendRaw(HttpRequest request) throws IOException, InterruptedException {
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private ConsorsbankApiException toApiException(HttpResponse<String> response) {
    String body = response.body() == null ? "" : response.body();
    try {
      ApiErrorResponse error = JsonSupport.objectMapper().readValue(body, ApiErrorResponse.class);
      return new ConsorsbankApiException(response.statusCode(), error, body);
    } catch (Exception ignored) {
      return new ConsorsbankApiException(response.statusCode(), null, body);
    }
  }

  private CreatedResource extractReference(HttpResponse<String> response) {
    if (response.statusCode() >= 400) {
      throw toApiException(response);
    }

    String location = response.headers().firstValue("Location").orElse(null);
    String body = response.body() == null ? "" : response.body().trim();
    String identifier = extractIdentifier(location, body);

    return CreatedResource.builder()
        .id(identifier)
        .no(identifier)
        .links(location == null ? List.of() : List.of())
        .build();
  }

  private String extractIdentifier(String location, String body) {
    if (location != null && !location.isBlank()) {
      int lastSlash = location.lastIndexOf('/');
      return lastSlash >= 0 ? location.substring(lastSlash + 1) : location;
    }

    if (body.startsWith("{") && body.endsWith("}")) {
      try {
        Map<?, ?> content = JsonSupport.objectMapper().readValue(body, Map.class);
        Object id = content.get("id");
        if (id != null) {
          return id.toString();
        }
        Object no = content.get("no");
        if (no != null) {
          return no.toString();
        }
      } catch (Exception ignored) {
      }
    }

    return body.replace("\"", "");
  }

  private String toQueryString(Map<String, ?> queryParameters) {
    if (queryParameters == null || queryParameters.isEmpty()) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    queryParameters.forEach((key, value) -> appendQueryParameter(builder, key, value));
    return builder.toString();
  }

  private void appendQueryParameter(StringBuilder builder, String key, Object value) {
    if (value == null) {
      return;
    }

    if (value instanceof Iterable<?> iterable) {
      for (Object item : iterable) {
        appendQueryParameter(builder, key, item);
      }
      return;
    }

    if (!builder.isEmpty()) {
      builder.append('&');
    }

    builder.append(encode(key)).append('=').append(encode(value.toString()));
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
