package io.github.makki93.consorsbank.mcp.tools;

import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.github.makki93.consorsbank.mcp.model.accounts.AccountCollection;
import io.github.makki93.consorsbank.mcp.model.accounts.PositionCollection;
import io.github.makki93.consorsbank.mcp.model.accounts.PositionHistoryCollection;
import io.github.makki93.consorsbank.mcp.model.accounts.SecuritiesAccountCollection;
import io.github.makki93.consorsbank.mcp.model.accounts.SecuritiesAccountPerformance;
import io.github.makki93.consorsbank.mcp.util.RequestArguments;
import io.github.makki93.consorsbank.mcp.util.Schemas;
import io.github.makki93.consorsbank.mcp.util.ToolResults;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AccountTools {
  private AccountTools() {
  }

  public static List<SyncToolSpecification> specifications(ConsorsbankHttpClient httpClient) {
    return List.of(
        tool(
            "get_securities_accounts",
            "List securities accounts with optional filters.",
            Schemas.object(
                Map.of(
                    "no", Schemas.string("Optional securities account number filter."),
                    "owner", Schemas.string("Optional owner name filter.")),
                List.of()),
            arguments -> {
              Map<String, Object> query = new LinkedHashMap<>();
              query.put("no", RequestArguments.optionalString(arguments, "no"));
              query.put("owner", RequestArguments.optionalString(arguments, "owner"));
              SecuritiesAccountCollection response = httpClient.get(
                  "/v1/securities-accounts",
                  query,
                  SecuritiesAccountCollection.class);
              return ToolResults.json(response);
            }),
        tool(
            "get_clearing_accounts",
            "List clearing accounts for a securities account.",
            Schemas.object(Map.of("no", Schemas.string("Securities account number.")), List.of("no")),
            arguments -> ToolResults.json(httpClient.get(
                "/v1/securities-accounts/" + RequestArguments.requiredString(arguments, "no") + "/accounts",
                AccountCollection.class))),
        tool(
            "get_positions",
            "List positions for a securities account with optional paging and filters.",
            Schemas.object(
                Map.of(
                    "no", Schemas.string("Securities account number."),
                    "wkn", Schemas.string("Optional WKN filter."),
                    "perPage", Schemas.integer("Optional page size."),
                    "page", Schemas.integer("Optional page number."),
                    "sort", Schemas.array("Optional sort values.", Map.of("type", "string"))),
                List.of("no")),
            arguments -> {
              Map<String, Object> query = new LinkedHashMap<>();
              query.put("wkn", RequestArguments.optionalString(arguments, "wkn"));
              query.put("perPage", RequestArguments.optionalInteger(arguments, "perPage"));
              query.put("page", RequestArguments.optionalInteger(arguments, "page"));
              query.put("sort", RequestArguments.optionalStringList(arguments, "sort"));
              PositionCollection response = httpClient.get(
                  "/v1/securities-accounts/" + RequestArguments.requiredString(arguments, "no") + "/positions",
                  query,
                  PositionCollection.class);
              return ToolResults.json(response);
            }),
        tool(
            "get_positions_history",
            "List position history entries for a securities account and WKN.",
            Schemas.object(
                Map.of(
                    "no", Schemas.string("Securities account number."),
                    "wkn", Schemas.string("Required WKN filter.")),
                List.of("no", "wkn")),
            arguments -> ToolResults.json(httpClient.get(
                "/v1/securities-accounts/" + RequestArguments.requiredString(arguments, "no") + "/positions-histories",
                Map.of("wkn", RequestArguments.requiredString(arguments, "wkn")),
                PositionHistoryCollection.class))),
        tool(
            "get_performance",
            "Return the portfolio performance summary for a securities account.",
            Schemas.object(Map.of("no", Schemas.string("Securities account number.")), List.of("no")),
            arguments -> ToolResults.json(httpClient.get(
                "/v1/securities-accounts/" + RequestArguments.requiredString(arguments, "no") + "/performance",
                SecuritiesAccountPerformance.class))));
  }

  private static SyncToolSpecification tool(
      String name,
      String description,
      JsonSchema inputSchema,
      ToolExecutor executor) {
    return SyncToolSpecification.builder()
        .tool(Tool.builder().name(name).description(description).inputSchema(inputSchema).build())
        .callHandler((exchange, request) -> {
          try {
            return executor.execute(request.arguments());
          } catch (Exception exception) {
            return ToolResults.error(exception.getMessage());
          }
        })
        .build();
  }

  @FunctionalInterface
  private interface ToolExecutor {
    CallToolResult execute(Map<String, Object> arguments) throws Exception;
  }
}
