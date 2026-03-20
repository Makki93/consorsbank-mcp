package io.github.makki93.consorsbank.mcp.model.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
  private String buyQuotation;
  private String currency;
  private String maxBuyAmount;
  private String maxSellAmount;
  private String quoteTimestamp;
  private String sellQuotation;
}
