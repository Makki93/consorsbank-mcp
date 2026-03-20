package io.github.makki93.consorsbank.mcp.model.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExAnteCost {
  private Object costDocument;
  private String currency;
  private String exAnteCostId;
  private String footnote;
  private String remark;
}
