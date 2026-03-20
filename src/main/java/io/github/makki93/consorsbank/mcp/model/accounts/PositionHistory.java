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
public class PositionHistory {
  private String direction;
  private String entryDescription;
  private String initialPrice;
  private String initialValue;
  private String isin;
  private List<Link> links;
  private String nominalAmount;
  private String price;
  private String profitLossAbsolut;
  private String purchaseDate;
  private String securityName;
  private String wkn;
}
