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
public class OrderModelCollection {
  private List<OrderModel> orderModels;
  private String orderType;
}
