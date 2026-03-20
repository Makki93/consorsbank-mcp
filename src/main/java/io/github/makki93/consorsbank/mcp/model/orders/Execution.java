package io.github.makki93.consorsbank.mcp.model.orders;

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
public class Execution {
  private String currency;
  private String executionAmount;
  private String executionPrice;
  private List<Link> links;
  private String timestamp;
  private String valueDate;
}
