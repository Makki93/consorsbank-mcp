package io.github.makki93.consorsbank.mcp.model.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedResource {
  private String no;
  private String id;
  private List<Link> links;
}
