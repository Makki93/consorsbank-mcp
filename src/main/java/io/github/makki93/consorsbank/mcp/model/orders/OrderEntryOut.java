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
public class OrderEntryOut {
  private String accountNo;
  private String direction;
  private String drippingQuantity;
  private String entityState;
  private String exAnteCostId;
  private String isin;
  private String limit;
  private String limitToken;
  private List<Link> links;
  private String marketPlaceId;
  private NextOrderOut nextOrder;
  private String no;
  private String nominalAmount;
  private String orderSupplement;
  private String overrideRiskClass;
  private String positionId;
  private Long secondsUntilExpiration;
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
