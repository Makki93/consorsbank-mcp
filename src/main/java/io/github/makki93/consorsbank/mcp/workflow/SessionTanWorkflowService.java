package io.github.makki93.consorsbank.mcp.workflow;

import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.github.makki93.consorsbank.mcp.model.auth.AuthenticationData;
import io.github.makki93.consorsbank.mcp.model.auth.ProfileTransactionStateOut;
import io.github.makki93.consorsbank.mcp.model.auth.SessionTanStatus;
import io.github.makki93.consorsbank.mcp.model.common.Challenge;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SessionTanWorkflowService {
  private final ConsorsbankHttpClient httpClient;

  public AuthenticationData getAuthenticationData() throws IOException, InterruptedException {
    return httpClient.get("/v1/profile/authentication-data", AuthenticationData.class);
  }

  public SessionTanStatus activate(String authType, String authCode) throws IOException, InterruptedException {
    return httpClient.post(
        "/v1/profile/session-tan",
        Challenge.builder().type(authType).code(authCode).build(),
        SessionTanStatus.class);
  }

  public SessionTanStatus deactivate() throws IOException, InterruptedException {
    return httpClient.delete("/v1/profile/session-tan", SessionTanStatus.class);
  }

  public ProfileTransactionStateOut getProfileTransactionState(String id) throws IOException, InterruptedException {
    return httpClient.get("/v1/profile-transaction-states/" + id, ProfileTransactionStateOut.class);
  }
}
