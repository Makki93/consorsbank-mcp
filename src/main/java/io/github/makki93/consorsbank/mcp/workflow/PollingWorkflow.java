package io.github.makki93.consorsbank.mcp.workflow;

import io.github.makki93.consorsbank.mcp.config.AppConfig;
import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.github.makki93.consorsbank.mcp.model.orders.OrderTransactionState;
import io.github.makki93.consorsbank.mcp.model.workflow.TransactionSummary;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PollingWorkflow {
  private static final Set<String> TERMINAL_STATES = Set.of(
      "AUTHORIZATION_ACCEPTED_DONE",
      "AUTHORIZATION_ERROR",
      "AUTHORIZATION_REJECTED",
      "AUTHORIZATION_USER_OFFLINE",
      "AUTHORIZATION_USER_TIMEOUT",
      "EXECUTION_SUCCESSFUL",
      "EXECUTION_FAILED",
      "EXECUTION_TIMED_OUT");

  private final ConsorsbankHttpClient httpClient;
  private final AppConfig appConfig;

  public TransactionSummary pollOrderTransaction(String transactionId) throws IOException, InterruptedException {
    long delayMillis = appConfig.pollInterval().toMillis();

    for (int attempt = 1; attempt <= appConfig.maxPollAttempts(); attempt++) {
      OrderTransactionState transactionState = httpClient.get(
          "/v1/order-transaction-states/" + transactionId,
          OrderTransactionState.class);

      if (isTerminal(transactionState.getState())) {
        return TransactionSummary.from(transactionState);
      }

      Thread.sleep(delayMillis);
      delayMillis = Math.min(delayMillis * 2, appConfig.requestTimeout().toMillis());
    }

    throw new IllegalStateException("Order transaction polling timed out for transaction " + transactionId);
  }

  private boolean isTerminal(String state) {
    return state != null && TERMINAL_STATES.contains(state);
  }
}
