# OpenClaw Setup

This guide shows how to run `consorsbank-mcp` from OpenClaw without storing the raw Consorsbank token in git-tracked files.

## Recommended rollout

Start with the safest setup first:

1. Build and test the server locally.
2. Run it in `read-only` mode.
3. Target `sandbox` first.
4. Use `CONSORS_ACCESS_TOKEN_FILE` or `CONSORS_ACCESS_TOKEN_COMMAND` instead of a raw token in config.
5. Assign this MCP server only to the specific OpenClaw agent that needs it.

## Build the server

From the repository root:

```bash
mvn test
```

## OpenClaw server entry

The examples below use `sh -lc` so the process starts in the repository directory and stays on `stdio`.

Replace `/absolute/path/to/consorsbank-mcp` with your local checkout path.

### Read-only sandbox with token file

```json
{
  "mcpServers": {
    "consorsbank-sandbox-readonly": {
      "command": "sh",
      "args": [
        "-lc",
        "cd /absolute/path/to/consorsbank-mcp && mvn -q exec:java"
      ],
      "transport": "stdio",
      "env": {
        "CONSORS_MODE": "read-only",
        "CONSORS_TARGET_ENV": "sandbox",
        "CONSORS_PROD_BASE_URL": "https://api.consorsbank.de/trading",
        "CONSORS_SANDBOX_BASE_URL": "https://api.consorsbank.de/sandbox/trading",
        "CONSORS_ACCESS_TOKEN_FILE": "/absolute/path/to/secrets/consorsbank-sandbox.token"
      }
    }
  }
}
```

### Full production with token command

```json
{
  "mcpServers": {
    "consorsbank-prod-full": {
      "command": "sh",
      "args": [
        "-lc",
        "cd /absolute/path/to/consorsbank-mcp && mvn -q exec:java"
      ],
      "transport": "stdio",
      "env": {
        "CONSORS_MODE": "full",
        "CONSORS_TARGET_ENV": "prod",
        "CONSORS_PROD_BASE_URL": "https://api.consorsbank.de/trading",
        "CONSORS_SANDBOX_BASE_URL": "https://api.consorsbank.de/sandbox/trading",
        "CONSORS_ACCESS_TOKEN_COMMAND": "security find-generic-password -a \"$USER\" -s consorsbank-prod-token -w"
      }
    }
  }
}
```

### Raw env token for local-only experiments

This is the least safe option. Use it only for short local sessions when the stronger file or command approach is not available.

```json
{
  "mcpServers": {
    "consorsbank-local-dev": {
      "command": "sh",
      "args": [
        "-lc",
        "cd /absolute/path/to/consorsbank-mcp && mvn -q exec:java"
      ],
      "transport": "stdio",
      "env": {
        "CONSORS_MODE": "read-only",
        "CONSORS_TARGET_ENV": "sandbox",
        "CONSORS_PROD_BASE_URL": "https://api.consorsbank.de/trading",
        "CONSORS_SANDBOX_BASE_URL": "https://api.consorsbank.de/sandbox/trading",
        "CONSORS_ACCESS_TOKEN": "${CONSORS_ACCESS_TOKEN}"
      }
    }
  }
}
```

## What the new settings mean

- `CONSORS_MODE=read-only` exposes only safe discovery and non-mutating tools.
- `CONSORS_MODE=full` exposes the complete trading and session workflow surface.
- `CONSORS_TARGET_ENV=sandbox` selects the sandbox URL from `CONSORS_SANDBOX_BASE_URL`.
- `CONSORS_TARGET_ENV=prod` selects the production URL from `CONSORS_PROD_BASE_URL`.
- `CONSORS_ACCESS_TOKEN_FILE` reads the token from a local file at startup.
- `CONSORS_ACCESS_TOKEN_COMMAND` runs a local command at startup and uses its stdout as the token.

Configure exactly one of these token sources:

- `CONSORS_ACCESS_TOKEN`
- `CONSORS_ACCESS_TOKEN_FILE`
- `CONSORS_ACCESS_TOKEN_COMMAND`

If more than one is set, the server fails fast instead of guessing.

## Agent scoping

Do not attach this MCP server globally if only one agent needs it.

Example:

```json
{
  "agents": {
    "trading-readonly": {
      "mcpServers": ["consorsbank-sandbox-readonly"]
    }
  }
}
```

This keeps other agents from seeing the Consorsbank tool catalog and reduces unnecessary tool overhead.

## Security notes

- Prefer `read-only` mode unless you actively need trading or session mutation tools.
- Prefer `sandbox` until you have completed the full validation checklist.
- Prefer token file or token command over raw token values in OpenClaw config.
- Treat OpenClaw config backups as sensitive because they may still reveal file paths or command names.
- The token is still available to the local MCP server process, so this is stronger than plain env storage but not the strictest possible isolation model.

## Validate the setup

After updating OpenClaw:

1. Restart the OpenClaw gateway.
2. Confirm the MCP server is running.
3. Start with `get_server_info`.
4. Verify the reported mode and target environment before using any other tool.
5. Follow `docs/sandbox-validation.md` for the detailed sandbox checklist.

## Future hardening

The stricter follow-up option is a separate local broker or proxy that owns the Consorsbank credential and performs outbound requests on behalf of the MCP server. That design is not implemented in this repository yet and should be treated as a future hardening track.
