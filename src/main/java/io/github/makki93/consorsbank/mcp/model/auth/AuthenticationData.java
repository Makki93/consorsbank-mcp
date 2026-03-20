package io.github.makki93.consorsbank.mcp.model.auth;

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
public class AuthenticationData {
  private List<AuthMethod> authMethods;
  private List<Link> links;
  private String login;
}
