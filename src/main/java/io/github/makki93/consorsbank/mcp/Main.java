package io.github.makki93.consorsbank.mcp;

import io.github.makki93.consorsbank.mcp.config.AppConfig;
import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.github.makki93.consorsbank.mcp.tools.AccountTools;
import io.github.makki93.consorsbank.mcp.tools.AuthTools;
import io.github.makki93.consorsbank.mcp.tools.MarketTools;
import io.github.makki93.consorsbank.mcp.tools.OrderTools;
import io.github.makki93.consorsbank.mcp.tools.ServerTools;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;

public final class Main {
  private Main() {
  }

  public static void main(String[] args) {

    AppConfig config = AppConfig.fromEnvironment();
    ConsorsbankHttpClient httpClient = new ConsorsbankHttpClient(config);
    StdioServerTransportProvider transportProvider =
        new StdioServerTransportProvider(
            new JacksonMcpJsonMapper(JsonMapper.builder().findAndAddModules().build()));

    McpSyncServer server = McpServer.sync(transportProvider)
        .serverInfo("consorsbank-mcp", BuildInfo.VERSION)
        .instructions(
            """
            Consorsbank MCP server for account, market, and trading workflows.
            Both production and sandbox connections are supported through environment configuration.
            Use discovery and validation tools first, and treat all trading operations as safety-sensitive.
            """)
        .capabilities(ServerCapabilities.builder().tools(true).build())
        .tools(toolSpecifications(config, httpClient))
        .build();

    System.err.println("consorsbank-mcp started on stdio with Java " + Runtime.version());
    if (server == null) {
      throw new IllegalStateException("Failed to initialize MCP server");
    }
  }

  static List<SyncToolSpecification> toolSpecifications(AppConfig config, ConsorsbankHttpClient httpClient) {
    List<SyncToolSpecification> toolSpecifications = new ArrayList<>(ServerTools.specifications(config, httpClient));
    toolSpecifications.addAll(AccountTools.specifications(httpClient));

    if (config.accessMode() == AppConfig.AccessMode.READ_ONLY) {
      toolSpecifications.addAll(AuthTools.readOnlySpecifications(httpClient));
      toolSpecifications.addAll(MarketTools.readOnlySpecifications(httpClient));
      toolSpecifications.addAll(OrderTools.readOnlySpecifications(httpClient, config));
      return List.copyOf(toolSpecifications);
    }

    toolSpecifications.addAll(AuthTools.specifications(httpClient));
    toolSpecifications.addAll(MarketTools.specifications(httpClient));
    toolSpecifications.addAll(OrderTools.specifications(httpClient, config));
    return List.copyOf(toolSpecifications);
  }
}
