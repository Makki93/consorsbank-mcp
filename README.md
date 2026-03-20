# Consorsbank MCP

Java 21 Model Context Protocol (MCP) server for the Consorsbank Trading API.

This repository aims to provide a clean Java MCP server that exposes Consorsbank trading, account, market, and session workflows to MCP-compatible clients. The implementation is designed around the official Java MCP SDK and the Consorsbank Trading API rather than a generated OpenAPI mirror.

## Status

This project now includes a working Java 21 MCP server scaffold with grouped tools for auth/session, accounts, market data, and order workflows.

Current MVP coverage:

- Java 21 build enforced through Maven
- `stdio` transport first, with streamable HTTP kept as a follow-up
- production-targeted configuration by default, with sandbox available as an explicit override
- shared Consorsbank HTTP client with normalized API error mapping
- Lombok-backed DTOs for the current supported endpoint surface
- grouped MCP tools for accounts, positions, performance, quotes, trading venues, orders, ex-ante costs, and transaction-state polling

## Why This Project Exists

Consorsbank already offers a substantial trading API, but there is not an MCP server in Java that packages the most useful trading and portfolio workflows into a tool-oriented interface for AI clients and local automation.

This repository is intended to:

- provide a Java-native MCP implementation
- map Consorsbank's asynchronous trading flows into ergonomic MCP tools
- make account and trading operations easier to orchestrate from MCP-aware environments
- stay explicit about safety-sensitive operations such as order placement and session TAN handling

## Scope

The current MVP is focused on the trading core rather than the entire API surface.

Implemented tool groups:

- Auth and session
  - authentication data
  - session level
  - session level elevation
  - session TAN activation and deactivation
  - profile transaction state polling
- Accounts and portfolio
  - securities accounts
  - clearing accounts
  - positions
  - position history
  - performance
- Market and reference data
  - trading venues
  - quotes
  - ex-ante costs
- Orders and workflows
  - order list and order details
  - create and update order entries
  - place order entries with transaction authentication
  - create and update order changes
  - place order changes
  - create, update, and accept quote order entries
  - cancel orders
  - poll order transaction states until terminal or timeout

Out of scope for the first milestone:

- complete OpenAPI coverage
- savings plan workflows
- production-grade OAuth callback hosting inside the MCP process
- advanced deployment packaging

## Architecture Direction

The current design centers on a few clear layers:

- `config`: environment parsing, runtime configuration, base URL selection
- `http`: typed Consorsbank client, shared headers, HAL/JSON handling, normalized error mapping
- `model`: request and response DTOs for the supported API surface
- `tools`: MCP tool registrations grouped by domain
- `workflow`: multi-step trading and polling workflows
- root bootstrap: MCP server startup and `stdio` transport wiring in `Main`

This keeps the codebase explicit and maintainable instead of relying on a large amount of generated code.

## Consorsbank Workflow Model

A few API characteristics shape the server design:

1. Trading is workflow-based, not single-call based.
2. Order placement commonly follows `create draft -> place -> poll transaction state`.
3. Transaction authentication depends on the user's configured methods such as `tan`, `session`, or `secureMessage`.
4. Session TAN matters for automation, but not all account setups allow the same degree of unattended execution.
5. Some operations complete asynchronously and use transaction-state resources rather than immediate final order results.

The MCP server will expose these as explicit tools and workflow helpers rather than hiding them behind ambiguous "do everything" operations.

## Runtime And Dependencies

Current baseline:

- Java 21
- Maven
- official Java MCP SDK
- JDK `HttpClient`
- Jackson for JSON handling
- Lombok for DTO and helper boilerplate reduction

The project is intentionally enforced to Java 21 in the Maven build.

## Planned Configuration

Expected environment variables will include values along these lines:

```bash
CONSORS_SANDBOX=false
CONSORS_BASE_URL=https://api.consorsbank.de/trading
CONSORS_CLIENT_ID=your-client-id
CONSORS_CLIENT_SECRET=your-client-secret
CONSORS_REDIRECT_URI=http://localhost:8787/callback
CONSORS_ACCESS_TOKEN=
CONSORS_REFRESH_TOKEN=
CONSORS_REQUEST_TIMEOUT_SECONDS=30
CONSORS_POLL_INTERVAL_MILLIS=1500
CONSORS_MAX_POLL_ATTEMPTS=10
```

The exact names may evolve slightly during implementation, but the server will remain environment-driven. Production is the default target; set `CONSORS_SANDBOX=true` only when you explicitly want the sandbox API.

A starter configuration template is included in [`.env.example`](.env.example).

## Tool Catalog

Current MCP tools include:

- `get_server_info`
- `ping_consorsbank_api`
- `get_authentication_data`
- `get_session_level`
- `elevate_session_level`
- `activate_session_tan`
- `deactivate_session_tan`
- `get_profile_transaction_state`
- `get_securities_accounts`
- `get_clearing_accounts`
- `get_positions`
- `get_positions_history`
- `get_performance`
- `get_trading_venues`
- `get_quote`
- `get_ex_ante_cost`
- `get_orders`
- `get_order`
- `create_order_entry`
- `update_order_entry`
- `place_order_entry`
- `create_and_place_order_entry`
- `create_order_change`
- `update_order_change`
- `place_order_change`
- `cancel_order`
- `create_quote_order_entry`
- `update_quote_order_entry`
- `accept_quote_order_entry`
- `poll_order_transaction_state`
- `get_order_transaction_state`

## Local Run

Run the server locally with Java 21:

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.7-graal"
export MAVEN_HOME="$HOME/.sdkman/candidates/maven/current"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
cp .env.example .env
mvn test
mvn exec:java
```

For sandbox validation, set these values before starting the server:

```bash
export CONSORS_SANDBOX=true
export CONSORS_BASE_URL=https://api.consorsbank.de/sandbox/trading
export CONSORS_ACCESS_TOKEN=your-sandbox-access-token
```

See [`docs/sandbox-validation.md`](docs/sandbox-validation.md) for a minimal smoke-test checklist.

## Safety And Compliance Notes

- This project is unofficial and is not affiliated with or endorsed by Consorsbank.
- You are responsible for complying with Consorsbank's API terms, trading rules, and all applicable regulations.
- Automated trading is risky. This software is not financial advice.
- Order placement and session TAN flows can have real financial effects. Review all configuration and workflow assumptions carefully before using real credentials or real money.

## License

This repository is public and source-available, but it is not intended to be used in commercial or corporate products without explicit permission from the repository owner.

See [`LICENSE`](LICENSE) for the exact terms.

Important summary:

- personal, educational, research, and evaluation use is allowed
- modification and non-commercial redistribution are allowed under the license terms
- commercial, corporate, SaaS, client-delivery, or productized use requires prior written permission

## Repository Files

Standard repository files included here:

- `README.md` for project overview and onboarding
- `LICENSE` for usage permissions and restrictions
- `CONTRIBUTING.md` for contribution expectations
- `SECURITY.md` for responsible disclosure guidance
- `CODE_OF_CONDUCT.md` for community expectations
- `.gitignore` for Java/Maven/editor hygiene
- `.env.example` for planned local configuration
- `.editorconfig` for baseline formatting consistency

## Contributing

Contributions are welcome, especially around:

- Consorsbank endpoint modeling
- MCP tool ergonomics
- safe trading workflow abstractions
- documentation improvements
- test coverage

Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) before opening a pull request.

## Security

If you discover a security issue, please follow the guidance in [`SECURITY.md`](SECURITY.md) and avoid publishing sensitive details in a public issue.

## References

- [Consorsbank Trading API: How to use the API](https://developer.consorsbank.de/documentation/docs/how-to-use-the-trading-api)
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Model Context Protocol Java SDK](https://github.com/modelcontextprotocol/java-sdk)

## Next Milestone

Likely next steps after the current MVP:

- add deeper endpoint coverage beyond the trading core
- add concrete MCP client configuration examples
- introduce streamable HTTP transport
- expand automated tests around tool inputs and HTTP error handling
