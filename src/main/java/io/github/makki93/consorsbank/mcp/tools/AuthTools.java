package io.github.makki93.consorsbank.mcp.tools;

import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.github.makki93.consorsbank.mcp.model.auth.SessionLevelOut;
import io.github.makki93.consorsbank.mcp.model.common.Challenge;
import io.github.makki93.consorsbank.mcp.util.RequestArguments;
import io.github.makki93.consorsbank.mcp.util.Schemas;
import io.github.makki93.consorsbank.mcp.util.ToolResults;
import io.github.makki93.consorsbank.mcp.workflow.SessionTanWorkflowService;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.util.List;
import java.util.Map;

public final class AuthTools {
  private AuthTools() {
  }

  public static List<SyncToolSpecification> specifications(ConsorsbankHttpClient httpClient) {
    SessionTanWorkflowService sessionTanWorkflowService = new SessionTanWorkflowService(httpClient);

    return List.of(
        tool(
            "get_authentication_data",
            "Return supported Consorsbank authentication methods for the current user.",
            Schemas.object(Map.of(), List.of()),
            arguments -> ToolResults.json(sessionTanWorkflowService.getAuthenticationData())),
        tool(
            "get_session_level",
            "Return the current Consorsbank session trust level.",
            Schemas.object(Map.of(), List.of()),
            arguments -> ToolResults.json(httpClient.get("/v1/profile/session-level", SessionLevelOut.class))),
        tool(
            "elevate_session_level",
            "Elevate the Consorsbank session level using a transaction authentication challenge.",
            Schemas.object(
                Map.of(
                    "type", Schemas.string("Authentication type, for example tan, session, or secureMessage."),
                    "code", Schemas.string("Authentication code if required by the selected type.")),
                List.of("type")),
            arguments -> ToolResults.json(httpClient.post(
                "/v1/profile/session-level-elevate",
                Challenge.builder()
                    .type(RequestArguments.requiredString(arguments, "type"))
                    .code(RequestArguments.optionalString(arguments, "code"))
                    .build(),
                Object.class))),
        tool(
            "activate_session_tan",
            "Activate Session TAN for the current Consorsbank session.",
            Schemas.object(
                Map.of(
                    "type", Schemas.string("Authentication type, for example tan or secureMessage."),
                    "code", Schemas.string("Authentication code if required by the selected type.")),
                List.of("type")),
            arguments -> ToolResults.json(sessionTanWorkflowService.activate(
                RequestArguments.requiredString(arguments, "type"),
                RequestArguments.optionalString(arguments, "code")))),
        tool(
            "deactivate_session_tan",
            "Deactivate Session TAN for the current Consorsbank session.",
            Schemas.object(Map.of(), List.of()),
            arguments -> ToolResults.json(sessionTanWorkflowService.deactivate())),
        tool(
            "get_profile_transaction_state",
            "Return a profile transaction state by id.",
            Schemas.object(Map.of("id", Schemas.string("Profile transaction id.")), List.of("id")),
            arguments -> ToolResults.json(sessionTanWorkflowService.getProfileTransactionState(
                RequestArguments.requiredString(arguments, "id")))));
  }

  public static List<SyncToolSpecification> readOnlySpecifications(ConsorsbankHttpClient httpClient) {
    SessionTanWorkflowService sessionTanWorkflowService = new SessionTanWorkflowService(httpClient);

    return List.of(
        tool(
            "get_authentication_data",
            "Return supported Consorsbank authentication methods for the current user.",
            Schemas.object(Map.of(), List.of()),
            arguments -> ToolResults.json(sessionTanWorkflowService.getAuthenticationData())),
        tool(
            "get_session_level",
            "Return the current Consorsbank session trust level.",
            Schemas.object(Map.of(), List.of()),
            arguments -> ToolResults.json(httpClient.get("/v1/profile/session-level", SessionLevelOut.class))),
        tool(
            "get_profile_transaction_state",
            "Return a profile transaction state by id.",
            Schemas.object(Map.of("id", Schemas.string("Profile transaction id.")), List.of("id")),
            arguments -> ToolResults.json(sessionTanWorkflowService.getProfileTransactionState(
                RequestArguments.requiredString(arguments, "id")))));
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
