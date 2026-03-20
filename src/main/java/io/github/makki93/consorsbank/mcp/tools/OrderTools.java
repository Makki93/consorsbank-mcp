package io.github.makki93.consorsbank.mcp.tools;

import io.github.makki93.consorsbank.mcp.config.AppConfig;
import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.github.makki93.consorsbank.mcp.model.common.Challenge;
import io.github.makki93.consorsbank.mcp.model.common.CreatedResource;
import io.github.makki93.consorsbank.mcp.model.orders.Order;
import io.github.makki93.consorsbank.mcp.model.orders.OrderChangeIn;
import io.github.makki93.consorsbank.mcp.model.orders.OrderChangeOut;
import io.github.makki93.consorsbank.mcp.model.orders.OrderCollection;
import io.github.makki93.consorsbank.mcp.model.orders.OrderEntryIn;
import io.github.makki93.consorsbank.mcp.model.orders.OrderEntryOut;
import io.github.makki93.consorsbank.mcp.model.orders.OrderTransactionState;
import io.github.makki93.consorsbank.mcp.model.orders.QuoteOrderEntryIn;
import io.github.makki93.consorsbank.mcp.model.orders.QuoteOrderEntryOut;
import io.github.makki93.consorsbank.mcp.util.JsonSupport;
import io.github.makki93.consorsbank.mcp.util.RequestArguments;
import io.github.makki93.consorsbank.mcp.util.Schemas;
import io.github.makki93.consorsbank.mcp.util.ToolResults;
import io.github.makki93.consorsbank.mcp.workflow.OrderWorkflowService;
import io.github.makki93.consorsbank.mcp.workflow.PollingWorkflow;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OrderTools {
  private OrderTools() {
  }

  public static List<SyncToolSpecification> specifications(ConsorsbankHttpClient httpClient, AppConfig appConfig) {
    PollingWorkflow pollingWorkflow = new PollingWorkflow(httpClient, appConfig);
    OrderWorkflowService orderWorkflowService = new OrderWorkflowService(httpClient, pollingWorkflow);

    return List.of(
        tool(
            "get_orders",
            "List orders with optional filters.",
            Schemas.object(
                Map.of(
                    "no", Schemas.string("Optional order number."),
                    "securitiesAccountNo", Schemas.string("Optional securities account number."),
                    "wkn", Schemas.string("Optional WKN."),
                    "isin", Schemas.string("Optional ISIN."),
                    "tradingVenueId", Schemas.string("Optional trading venue id.")),
                List.of()),
            arguments -> {
              Map<String, Object> query = new LinkedHashMap<>();
              query.put("no", RequestArguments.optionalString(arguments, "no"));
              query.put("securitiesAccountNo", RequestArguments.optionalString(arguments, "securitiesAccountNo"));
              query.put("wkn", RequestArguments.optionalString(arguments, "wkn"));
              query.put("isin", RequestArguments.optionalString(arguments, "isin"));
              query.put("tradingVenueId", RequestArguments.optionalString(arguments, "tradingVenueId"));
              OrderCollection response = httpClient.get("/v1/orders", query, OrderCollection.class);
              return ToolResults.json(response);
            }),
        tool(
            "get_order",
            "Return a single order by order number.",
            Schemas.object(Map.of("no", Schemas.string("Order number.")), List.of("no")),
            arguments -> ToolResults.json(httpClient.get(
                "/v1/orders/" + RequestArguments.requiredString(arguments, "no"),
                Order.class))),
        tool(
            "create_order_entry",
            "Create an order entry draft without placing it.",
            orderEntrySchema(),
            arguments -> {
              OrderEntryIn requestBody = JsonSupport.convertValue(arguments, OrderEntryIn.class);
              CreatedResource reference = orderWorkflowService.createOrderEntry(requestBody);
              return ToolResults.json(reference);
            }),
        tool(
            "update_order_entry",
            "Update an existing order entry draft.",
            mergeSchemas(orderEntrySchema(), Map.of("no", Schemas.string("Order entry number.")), List.of("no")),
            arguments -> {
              String no = RequestArguments.requiredString(arguments, "no");
              OrderEntryIn requestBody = JsonSupport.convertValue(without(arguments, "no"), OrderEntryIn.class);
              OrderEntryOut response = httpClient.put("/v1/order-entries/" + no, requestBody, OrderEntryOut.class);
              return ToolResults.json(response);
            }),
        tool(
            "place_order_entry",
            "Place an existing order entry draft and poll until the transaction reaches a terminal state.",
            challengeSchema("no", "Order entry number."),
            arguments -> ToolResults.json(orderWorkflowService.placeOrderEntry(
                RequestArguments.requiredString(arguments, "no"),
                toChallenge(arguments)))),
        tool(
            "create_and_place_order_entry",
            "Create an order entry draft, place it immediately, and poll until terminal state.",
            mergeSchemas(orderEntrySchema(), challengeSchemaProperties(), List.of("type")),
            arguments -> {
              OrderEntryIn requestBody = JsonSupport.convertValue(
                  filter(arguments, List.of("type", "code")),
                  OrderEntryIn.class);
              CreatedResource draft = orderWorkflowService.createOrderEntry(requestBody);
              return ToolResults.json(orderWorkflowService.placeOrderEntry(draft.getNo(), toChallenge(arguments)));
            }),
        tool(
            "create_order_change",
            "Create an order change draft from an existing order.",
            mergeSchemas(orderChangeSchema(), Map.of("orderNo", Schemas.string("Existing order number.")), List.of("orderNo")),
            arguments -> {
              String orderNo = RequestArguments.requiredString(arguments, "orderNo");
              OrderChangeIn requestBody = JsonSupport.convertValue(without(arguments, "orderNo"), OrderChangeIn.class);
              CreatedResource reference = orderWorkflowService.createOrderChange(orderNo, requestBody);
              return ToolResults.json(reference);
            }),
        tool(
            "update_order_change",
            "Update an existing order change draft.",
            mergeSchemas(orderChangeSchema(), Map.of("no", Schemas.string("Order change number.")), List.of("no")),
            arguments -> {
              String no = RequestArguments.requiredString(arguments, "no");
              OrderChangeIn requestBody = JsonSupport.convertValue(without(arguments, "no"), OrderChangeIn.class);
              OrderChangeOut response = httpClient.put("/v1/order-changes/" + no, requestBody, OrderChangeOut.class);
              return ToolResults.json(response);
            }),
        tool(
            "place_order_change",
            "Place an order change and poll until terminal state.",
            challengeSchema("no", "Order change number."),
            arguments -> ToolResults.json(orderWorkflowService.placeOrderChange(
                RequestArguments.requiredString(arguments, "no"),
                toChallenge(arguments)))),
        tool(
            "cancel_order",
            "Cancel an order and poll until terminal state.",
            challengeSchema("no", "Order number."),
            arguments -> ToolResults.json(orderWorkflowService.cancelOrder(
                RequestArguments.requiredString(arguments, "no"),
                toChallenge(arguments)))),
        tool(
            "create_quote_order_entry",
            "Create a quote order entry draft.",
            quoteOrderEntrySchema(),
            arguments -> ToolResults.json(orderWorkflowService.createQuoteOrderEntry(
                JsonSupport.convertValue(arguments, QuoteOrderEntryIn.class)))),
        tool(
            "update_quote_order_entry",
            "Update a quote order entry draft.",
            mergeSchemas(quoteOrderEntrySchema(), Map.of("no", Schemas.string("Quote order entry number.")), List.of("no")),
            arguments -> {
              String no = RequestArguments.requiredString(arguments, "no");
              QuoteOrderEntryIn requestBody = JsonSupport.convertValue(without(arguments, "no"), QuoteOrderEntryIn.class);
              QuoteOrderEntryOut response = httpClient.put(
                  "/v1/quote-order-entries/" + no,
                  requestBody,
                  QuoteOrderEntryOut.class);
              return ToolResults.json(response);
            }),
        tool(
            "accept_quote_order_entry",
            "Accept a quote order entry and poll until terminal state.",
            challengeSchema("no", "Quote order entry number."),
            arguments -> ToolResults.json(orderWorkflowService.acceptQuoteOrderEntry(
                RequestArguments.requiredString(arguments, "no"),
                toChallenge(arguments)))),
        tool(
            "poll_order_transaction_state",
            "Poll an order transaction state until it reaches a terminal state.",
            Schemas.object(Map.of("id", Schemas.string("Order transaction id.")), List.of("id")),
            arguments -> ToolResults.json(pollingWorkflow.pollOrderTransaction(
                RequestArguments.requiredString(arguments, "id")))),
        tool(
            "get_order_transaction_state",
            "Return the current order transaction state without polling.",
            Schemas.object(Map.of("id", Schemas.string("Order transaction id.")), List.of("id")),
            arguments -> ToolResults.json(httpClient.get(
                "/v1/order-transaction-states/" + RequestArguments.requiredString(arguments, "id"),
                OrderTransactionState.class))));
  }

  public static List<SyncToolSpecification> readOnlySpecifications(
      ConsorsbankHttpClient httpClient,
      AppConfig appConfig) {
    PollingWorkflow pollingWorkflow = new PollingWorkflow(httpClient, appConfig);

    return List.of(
        tool(
            "get_orders",
            "List orders with optional filters.",
            Schemas.object(
                Map.of(
                    "no", Schemas.string("Optional order number."),
                    "securitiesAccountNo", Schemas.string("Optional securities account number."),
                    "wkn", Schemas.string("Optional WKN."),
                    "isin", Schemas.string("Optional ISIN."),
                    "tradingVenueId", Schemas.string("Optional trading venue id.")),
                List.of()),
            arguments -> {
              Map<String, Object> query = new LinkedHashMap<>();
              query.put("no", RequestArguments.optionalString(arguments, "no"));
              query.put("securitiesAccountNo", RequestArguments.optionalString(arguments, "securitiesAccountNo"));
              query.put("wkn", RequestArguments.optionalString(arguments, "wkn"));
              query.put("isin", RequestArguments.optionalString(arguments, "isin"));
              query.put("tradingVenueId", RequestArguments.optionalString(arguments, "tradingVenueId"));
              OrderCollection response = httpClient.get("/v1/orders", query, OrderCollection.class);
              return ToolResults.json(response);
            }),
        tool(
            "get_order",
            "Return a single order by order number.",
            Schemas.object(Map.of("no", Schemas.string("Order number.")), List.of("no")),
            arguments -> ToolResults.json(httpClient.get(
                "/v1/orders/" + RequestArguments.requiredString(arguments, "no"),
                Order.class))),
        tool(
            "poll_order_transaction_state",
            "Poll an order transaction state until it reaches a terminal state.",
            Schemas.object(Map.of("id", Schemas.string("Order transaction id.")), List.of("id")),
            arguments -> ToolResults.json(pollingWorkflow.pollOrderTransaction(
                RequestArguments.requiredString(arguments, "id")))),
        tool(
            "get_order_transaction_state",
            "Return the current order transaction state without polling.",
            Schemas.object(Map.of("id", Schemas.string("Order transaction id.")), List.of("id")),
            arguments -> ToolResults.json(httpClient.get(
                "/v1/order-transaction-states/" + RequestArguments.requiredString(arguments, "id"),
                OrderTransactionState.class))));
  }

  private static JsonSchema orderEntrySchema() {
    return Schemas.object(
        Map.ofEntries(
            Map.entry("accountNo", Schemas.string("Order account number.")),
            Map.entry("direction", Schemas.string("BUY or SELL.")),
            Map.entry("nominalAmount", Schemas.string("Order amount.")),
            Map.entry("securitiesAccountNo", Schemas.string("Securities account number.")),
            Map.entry("tradingVenueId", Schemas.string("Trading venue id.")),
            Map.entry("isin", Schemas.string("Optional ISIN.")),
            Map.entry("wkn", Schemas.string("Optional WKN.")),
            Map.entry("limit", Schemas.string("Optional limit.")),
            Map.entry("marketPlaceId", Schemas.string("Optional market place id.")),
            Map.entry("orderSupplement", Schemas.string("Optional order supplement.")),
            Map.entry("overrideRiskClass", Schemas.string("Optional override risk class flag.")),
            Map.entry("positionId", Schemas.string("Optional position id.")),
            Map.entry("stop", Schemas.string("Optional stop trigger.")),
            Map.entry("stopLimit", Schemas.string("Optional stop limit.")),
            Map.entry("trailingDistance", Schemas.string("Optional trailing distance.")),
            Map.entry("trailingLimitTolerance", Schemas.string("Optional trailing limit tolerance.")),
            Map.entry("trailingNotation", Schemas.string("Optional trailing notation.")),
            Map.entry("validityDate", Schemas.string("Optional validity date."))),
        List.of("accountNo", "direction", "nominalAmount", "securitiesAccountNo", "tradingVenueId"));
  }

  private static JsonSchema orderChangeSchema() {
    return Schemas.object(
        Map.of(
            "accountNo", Schemas.string("Optional order account number."),
            "securitiesAccountNo", Schemas.string("Securities account number."),
            "limit", Schemas.string("Optional limit."),
            "orderSupplement", Schemas.string("Optional order supplement."),
            "stop", Schemas.string("Optional stop trigger."),
            "stopLimit", Schemas.string("Optional stop limit."),
            "trailingDistance", Schemas.string("Optional trailing distance."),
            "trailingLimitTolerance", Schemas.string("Optional trailing limit tolerance."),
            "trailingNotation", Schemas.string("Optional trailing notation."),
            "validityDate", Schemas.string("Optional validity date.")),
        List.of("securitiesAccountNo"));
  }

  private static JsonSchema quoteOrderEntrySchema() {
    return Schemas.object(
        Map.ofEntries(
            Map.entry("accountNo", Schemas.string("Order account number.")),
            Map.entry("direction", Schemas.string("BUY or SELL.")),
            Map.entry("marketPlaceId", Schemas.string("Market place id.")),
            Map.entry("nominalAmount", Schemas.string("Order amount.")),
            Map.entry("securitiesAccountNo", Schemas.string("Securities account number.")),
            Map.entry("tradingVenueId", Schemas.string("Trading venue id.")),
            Map.entry("isin", Schemas.string("Optional ISIN.")),
            Map.entry("wkn", Schemas.string("Optional WKN.")),
            Map.entry("limit", Schemas.string("Optional limit.")),
            Map.entry("overrideRiskClass", Schemas.string("Optional override risk class flag.")),
            Map.entry("positionId", Schemas.string("Optional position id."))),
        List.of("accountNo", "direction", "marketPlaceId", "nominalAmount", "securitiesAccountNo", "tradingVenueId"));
  }

  private static JsonSchema challengeSchema(String key, String description) {
    return mergeSchemas(
        Schemas.object(challengeSchemaProperties(), List.of("type")),
        Map.of(key, Schemas.string(description)),
        List.of(key, "type"));
  }

  private static Map<String, Object> challengeSchemaProperties() {
    return Map.of(
        "type", Schemas.string("Authentication type, for example tan, session, or secureMessage."),
        "code", Schemas.string("Authentication code if required by the chosen type."));
  }

  private static JsonSchema mergeSchemas(
      JsonSchema base,
      Map<String, Object> extraProperties,
      List<String> extraRequired) {
    Map<String, Object> mergedProperties = new LinkedHashMap<>(base.properties());
    mergedProperties.putAll(extraProperties);
    List<String> mergedRequired = new ArrayList<>(base.required());
    mergedRequired.addAll(extraRequired);
    return Schemas.object(mergedProperties, mergedRequired.stream().distinct().toList());
  }

  private static Challenge toChallenge(Map<String, Object> arguments) {
    return Challenge.builder()
        .type(RequestArguments.requiredString(arguments, "type"))
        .code(RequestArguments.optionalString(arguments, "code"))
        .build();
  }

  private static Map<String, Object> without(Map<String, Object> arguments, String key) {
    return filter(arguments, List.of(key));
  }

  private static Map<String, Object> filter(Map<String, Object> arguments, List<String> excludedKeys) {
    Map<String, Object> filtered = new LinkedHashMap<>();
    arguments.forEach((key, value) -> {
      if (!excludedKeys.contains(key)) {
        filtered.put(key, value);
      }
    });
    return filtered;
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
