package io.github.makki93.consorsbank.mcp.tools;

import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.github.makki93.consorsbank.mcp.model.market.ExAnteCost;
import io.github.makki93.consorsbank.mcp.model.market.Quote;
import io.github.makki93.consorsbank.mcp.model.market.QuoteIn;
import io.github.makki93.consorsbank.mcp.model.market.TradingVenueCollection;
import io.github.makki93.consorsbank.mcp.util.JsonSupport;
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

public final class MarketTools {
  private MarketTools() {
  }

  public static List<SyncToolSpecification> specifications(ConsorsbankHttpClient httpClient) {
    return List.of(
        tool(
            "get_trading_venues",
            "List trading venues for a WKN with optional quote-based filtering.",
            Schemas.object(
                Map.of(
                    "wkn", Schemas.string("Security WKN."),
                    "isQuoteBased", Schemas.string("Optional quote based filter.")),
                List.of("wkn")),
            arguments -> {
              Map<String, Object> query = new LinkedHashMap<>();
              query.put("isQuoteBased", RequestArguments.optionalString(arguments, "isQuoteBased"));
              TradingVenueCollection response = httpClient.get(
                  "/v1/securities/" + RequestArguments.requiredString(arguments, "wkn") + "/tradingvenues",
                  query,
                  TradingVenueCollection.class);
              return ToolResults.json(response);
            }),
        tool(
            "get_quote",
            "Request a quote for a trading venue and market place.",
            Schemas.object(
                Map.of(
                    "direction", Schemas.string("BUY or SELL."),
                    "marketPlaceId", Schemas.string("Market place id."),
                    "nominalAmount", Schemas.string("Nominal amount."),
                    "tradingVenueId", Schemas.string("Trading venue id."),
                    "isin", Schemas.string("Optional ISIN."),
                    "wkn", Schemas.string("Optional WKN.")),
                List.of("direction", "marketPlaceId", "nominalAmount", "tradingVenueId")),
            arguments -> {
              QuoteIn requestBody = JsonSupport.convertValue(arguments, QuoteIn.class);
              Quote response = httpClient.post("/v1/quotes", requestBody, Quote.class);
              return ToolResults.json(response);
            }),
        tool(
            "get_ex_ante_cost",
            "Return an ex-ante cost document by id.",
            Schemas.object(Map.of("id", Schemas.string("Ex-ante cost id.")), List.of("id")),
            arguments -> ToolResults.json(httpClient.get(
                "/v1/ex-ante-costs/" + RequestArguments.requiredString(arguments, "id"),
                ExAnteCost.class))));
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
