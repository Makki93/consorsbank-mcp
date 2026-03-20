package io.github.makki93.consorsbank.mcp.model.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteIn {
  private String direction;
  private String isin;
  private String marketPlaceId;
  private String nominalAmount;
  private String tradingVenueId;
  private String wkn;
}
