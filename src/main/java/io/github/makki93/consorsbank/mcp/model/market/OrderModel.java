package io.github.makki93.consorsbank.mcp.model.market;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderModel {
  private Boolean isNextOrderPossible;
  private String orderRestriction;
  private List<OrderSupplementInfo> orderSupplementList;
}
