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
public class Order {
  private String accountNo;
  private Long baseOrderNo;
  private String cancellationTimestamp;
  private String currency;
  private String direction;
  private String drippingQuantity;
  private String exAnteCostId;
  private String executionPrice;
  private List<Execution> executions;
  private String isin;
  private String limit;
  private String limitToken;
  private List<Link> links;
  private Long nextOrderNo;
  private Long no;
  private String nominalAmount;
  private String orderSupplement;
  private String restriction;
  private String securitiesAccountNo;
  private String securityName;
  private String state;
  private String stop;
  private String stopLimit;
  private String timestamp;
  private String tradingVenueId;
  private String tradingVenueName;
  private String trailingDistance;
  private String trailingLimitTolerance;
  private String trailingNotation;
  private String validityDate;
  private String wkn;
}
