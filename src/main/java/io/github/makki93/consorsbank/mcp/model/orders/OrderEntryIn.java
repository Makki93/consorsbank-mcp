package io.github.makki93.consorsbank.mcp.model.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntryIn {
  private String accountNo;
  private String direction;
  private String drippingQuantity;
  private String isin;
  private String limit;
  private String marketPlaceId;
  private NextOrderIn nextOrder;
  private String nominalAmount;
  private String orderSupplement;
  private String overrideRiskClass;
  private String positionId;
  private String securitiesAccountNo;
  private String stop;
  private String stopLimit;
  private String tradingVenueId;
  private String trailingDistance;
  private String trailingLimitTolerance;
  private String trailingNotation;
  private String validityDate;
  private String wkn;
}
