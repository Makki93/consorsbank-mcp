package io.github.makki93.consorsbank.mcp.model.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderChangeIn {
  private String accountNo;
  private String drippingQuantity;
  private String limit;
  private String orderSupplement;
  private String securitiesAccountNo;
  private String stop;
  private String stopLimit;
  private String trailingDistance;
  private String trailingLimitTolerance;
  private String trailingNotation;
  private String validityDate;
}
