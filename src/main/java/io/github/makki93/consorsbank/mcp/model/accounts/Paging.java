package io.github.makki93.consorsbank.mcp.model.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paging {
  private Integer page;
  private Integer perPage;
  private Integer totalItems;
  private Integer totalPage;
}
