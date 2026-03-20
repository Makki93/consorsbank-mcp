package io.github.makki93.consorsbank.mcp.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Link {
  private String deprecation;
  private String href;
  private String hreflang;
  private String media;
  private String name;
  private String profile;
  private String rel;
  private String title;
  private String type;
}
