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
public class PositionHistoryCollection {
  private List<PositionHistory> items;
  private List<Link> links;
}
