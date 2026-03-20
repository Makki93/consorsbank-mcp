package io.github.makki93.consorsbank.mcp.model.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSupplementInfo {
  private Boolean mandatory;
  private String orderSupplement;
}
