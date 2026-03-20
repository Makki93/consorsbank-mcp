package io.github.makki93.consorsbank.mcp.model.market;

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
public class TradingVenue {
  private String currency;
  private Boolean defaultTradingVenue;
  private String goodForDay;
  private String goodTillCancelled;
  private String goodTillDate;
  private String goodTillUltimo;
  private Boolean homeTradingVenue;
  private Boolean limitBasedBuy;
  private Boolean limitBasedSell;
  private List<Link> links;
  private String marketPlaceIdForLimit;
  private String marketPlaceIdForQuote;
  private List<OrderModelCollection> orderModelCollection;
  private Boolean quoteBasedBuy;
  private Boolean quoteBasedSell;
  private String tradingVenueId;
  private String tradingVenueName;
}
