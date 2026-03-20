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
public class PositionCollection {
  private List<Position> items;
  private List<Link> links;
  private Paging paging;
}
