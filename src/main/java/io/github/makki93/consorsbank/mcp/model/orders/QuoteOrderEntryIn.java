package io.github.makki93.consorsbank.mcp.model.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteOrderEntryIn {
  private String accountNo;
  private String direction;
  private String isin;
  private String limit;
  private String marketPlaceId;
  private String nominalAmount;
  private String overrideRiskClass;
  private String positionId;
  private String securitiesAccountNo;
  private String tradingVenueId;
  private String wkn;
}
