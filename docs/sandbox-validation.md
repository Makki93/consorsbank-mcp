# Sandbox Validation

Use this checklist to validate the MVP against the Consorsbank sandbox without touching the production environment.

## Prerequisites

- Java 21 available locally
- Maven available locally
- sandbox credentials or sandbox access token
- `CONSORS_SANDBOX=true`

## Environment

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.7-graal"
export MAVEN_HOME="$HOME/.sdkman/candidates/maven/current"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

export CONSORS_SANDBOX=true
export CONSORS_BASE_URL=https://api.consorsbank.de/sandbox/trading
export CONSORS_ACCESS_TOKEN=your-sandbox-access-token
```

## Build And Start

```bash
mvn test
mvn exec:java
```

## Suggested Smoke Tests

Run these in order from an MCP client:

1. `get_server_info`
2. `ping_consorsbank_api`
3. `get_authentication_data`
4. `get_session_level`
5. `get_securities_accounts`
6. `get_positions`
7. `get_trading_venues`
8. `get_quote`

Only move on to order workflows once the read-side tools succeed and you understand the sandbox account state.

## Order Workflow Check

The intended asynchronous trading pattern is:

1. `create_order_entry`
2. `place_order_entry`
3. `poll_order_transaction_state`

The same pattern applies to order changes, quote order entries, and cancellations.

## Follow-Up

Streamable HTTP transport, broader endpoint coverage, and more exhaustive integration tests remain follow-up work beyond this MVP validation path.
