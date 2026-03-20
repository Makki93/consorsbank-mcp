package io.github.makki93.consorsbank.mcp.model.orders;

import io.github.makki93.consorsbank.mcp.model.common.Link;
import io.github.makki93.consorsbank.mcp.model.market.Quote;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteOrderEntryOut {
  private String accountNo;
  private String direction;
  private String entityState;
  private String exAnteCostId;
  private String isin;
  private String limit;
  private List<Link> links;
  private String marketMakerId;
  private String marketPlaceId;
  private String marketPlaceOrderNo;
  private String no;
  private String nominalAmount;
  private Long orderNo;
  private String overrideRiskClass;
  private String positionId;
  private Quote quote;
  private String securitiesAccountNo;
  private String securityName;
  private String timestamp;
  private String tradingVenueId;
  private String wkn;
}
