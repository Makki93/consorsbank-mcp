package io.github.makki93.consorsbank.mcp.model.market;

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
public class TradingVenueCollection {
  private String isin;
  private List<TradingVenue> items;
  private List<Link> links;
  private String wkn;
}
