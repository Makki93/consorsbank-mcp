package io.github.makki93.consorsbank.mcp.model.workflow;

import io.github.makki93.consorsbank.mcp.model.orders.OrderTransactionState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {
  private String transactionId;
  private String state;
  private Long orderNo;
  private Long nextOrderNo;

  public static TransactionSummary from(OrderTransactionState state) {
    return TransactionSummary.builder()
        .transactionId(state.getId())
        .state(state.getState())
        .orderNo(state.getOrderNo())
        .nextOrderNo(state.getNextOrderNo())
        .build();
  }
}
