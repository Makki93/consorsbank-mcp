package io.github.makki93.consorsbank.mcp.workflow;

import io.github.makki93.consorsbank.mcp.http.ConsorsbankHttpClient;
import io.github.makki93.consorsbank.mcp.model.common.Challenge;
import io.github.makki93.consorsbank.mcp.model.common.CreatedResource;
import io.github.makki93.consorsbank.mcp.model.orders.OrderChangeIn;
import io.github.makki93.consorsbank.mcp.model.orders.OrderEntryIn;
import io.github.makki93.consorsbank.mcp.model.orders.QuoteOrderEntryIn;
import io.github.makki93.consorsbank.mcp.model.workflow.TransactionSummary;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderWorkflowService {
  private final ConsorsbankHttpClient httpClient;
  private final PollingWorkflow pollingWorkflow;

  public CreatedResource createOrderEntry(OrderEntryIn orderEntryIn) throws IOException, InterruptedException {
    return httpClient.postForReference("/v1/order-entries", orderEntryIn);
  }

  public TransactionSummary placeOrderEntry(String orderEntryNo, Challenge challenge)
      throws IOException, InterruptedException {
    CreatedResource reference = httpClient.postForReference("/v1/order-entries/" + orderEntryNo + "/place", challenge);
    return pollingWorkflow.pollOrderTransaction(reference.getId());
  }

  public CreatedResource createOrderChange(String orderNo, OrderChangeIn orderChangeIn)
      throws IOException, InterruptedException {
    return httpClient.postForReference("/v1/orders/" + orderNo + "/change", orderChangeIn);
  }

  public TransactionSummary placeOrderChange(String orderChangeNo, Challenge challenge)
      throws IOException, InterruptedException {
    CreatedResource reference = httpClient.postForReference("/v1/order-changes/" + orderChangeNo + "/place", challenge);
    return pollingWorkflow.pollOrderTransaction(reference.getId());
  }

  public TransactionSummary cancelOrder(String orderNo, Challenge challenge)
      throws IOException, InterruptedException {
    CreatedResource reference = httpClient.postForReference("/v1/orders/" + orderNo + "/cancel", challenge);
    return pollingWorkflow.pollOrderTransaction(reference.getId());
  }

  public CreatedResource createQuoteOrderEntry(QuoteOrderEntryIn quoteOrderEntryIn)
      throws IOException, InterruptedException {
    return httpClient.postForReference("/v1/quote-order-entries", quoteOrderEntryIn);
  }

  public TransactionSummary acceptQuoteOrderEntry(String quoteOrderEntryNo, Challenge challenge)
      throws IOException, InterruptedException {
    CreatedResource reference = httpClient.postForReference(
        "/v1/quote-order-entries/" + quoteOrderEntryNo + "/accept",
        challenge);
    return pollingWorkflow.pollOrderTransaction(reference.getId());
  }
}
