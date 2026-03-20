package io.github.makki93.consorsbank.mcp.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionLevelOut {
  private Integer authTrustLevel;
}
