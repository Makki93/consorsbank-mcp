package io.github.makki93.consorsbank.mcp.model.accounts;

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
public class Position {
  private String assetClass;
  private String currency;
  private String custodianCountry;
  private String id;
  private String initialPrice;
  private String isin;
  private List<Link> links;
  private String lockState;
  private String nominalAmount;
  private String profitLossAbsolut;
  private String profitLossPercent;
  private String securityName;
  private String value;
  private String wkn;
}
