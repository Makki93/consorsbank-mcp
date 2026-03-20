package io.github.makki93.consorsbank.mcp.model.orders;

import io.github.makki93.consorsbank.mcp.model.common.Link;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderChangeOut {
  private String accountNo;
  private String drippingQuantity;
  private String entityState;
  private String exAnteCostId;
  private String limit;
  private List<Link> links;
  private String no;
  private String orderSupplement;
  private String securitiesAccountNo;
  private String stop;
  private String stopLimit;
  private String trailingDistance;
  private String trailingLimitTolerance;
  private String trailingNotation;
  private String validityDate;
}
