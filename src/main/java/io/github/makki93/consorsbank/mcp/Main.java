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
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
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
        .toolCall(ServerTools.serverInfoTool(), (exchange, request) -> ServerTools.serverInfo(config))
        .toolCall(
            ServerTools.pingConsorsbankTool(),
            (exchange, request) -> ServerTools.pingConsorsbank(httpClient))
        .tools(AuthTools.specifications(httpClient))
        .tools(AccountTools.specifications(httpClient))
        .tools(MarketTools.specifications(httpClient))
        .tools(OrderTools.specifications(httpClient, config))
        .build();

    System.err.println("consorsbank-mcp started on stdio with Java " + Runtime.version());
    if (server == null) {
      throw new IllegalStateException("Failed to initialize MCP server");
    }
  }
}
