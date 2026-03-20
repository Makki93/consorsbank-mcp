# Sandbox Validation

Use this guide to validate the server against the Consorsbank sandbox without accidentally drifting into production.

## Safety rules for a weaker LLM

Follow these rules exactly:

1. Never call any tool before `get_server_info`.
2. Never continue if `get_server_info` reports production when sandbox is expected.
3. Never continue if `get_server_info` reports `mode=full` when the task expects read-only validation.
4. Never invent values such as account numbers, WKNs, trading venue ids, order ids, or authentication challenge codes.
5. Only reuse values returned by earlier tool calls in the same session.
6. Never switch from read-only to full mode unless the human explicitly asks for order-flow testing.
7. Never reveal or print token values.
8. Stop immediately on any authentication, authorization, or environment mismatch.

## Prerequisites

- Java 21 available locally
- Maven available locally
- a sandbox access token available through exactly one configured token source
- OpenClaw configured with the MCP server entry from `docs/openclaw-setup.md`
- sandbox mode selected through `CONSORS_TARGET_ENV=sandbox`

## Local verification before OpenClaw

From the repository root:

```bash
mvn test
```

Expected result:

- build succeeds
- tests pass

If this fails, do not continue into OpenClaw.

## OpenClaw startup checks

1. Restart the OpenClaw gateway.
2. Confirm the Consorsbank MCP server is listed and running.
3. Confirm you attached the correct server variant to the correct agent:
   - read-only sandbox for discovery validation
   - full sandbox only for explicitly approved order-flow validation

Do not hand the server to an LLM until these checks pass.

## Phase 1: mandatory read-only sandbox validation

This phase must pass before any full-mode testing.

### Step 1: Verify runtime configuration

Call:

- `get_server_info`

Expected checks:

- reported target base URL is the sandbox URL
- reported mode is `read-only`
- access token is configured

Stop immediately if any of these checks fail.

### Step 2: Verify client wiring

Call:

- `ping_consorsbank_api`

Expected checks:

- tool returns the sandbox base URL
- tool returns an example endpoint path

This does not prove the remote API is reachable, but it does confirm the local client wiring and target selection.

### Step 3: Discover auth state

Call in order:

1. `get_authentication_data`
2. `get_session_level`

Expected checks:

- both calls succeed
- authentication methods are returned
- current session level is returned

If either call fails with unauthorized or forbidden, stop and fix credentials before continuing.

### Step 4: Discover sandbox accounts

Call:

1. `get_securities_accounts`

Expected checks:

- at least one securities account is returned, or the response clearly shows the sandbox has no usable account data

If no account number is available, stop here and ask the human for the next step.

### Step 5: Validate account read tools

Take one `securitiesAccountNo` or `no` value from the previous response and reuse it.

Call in order:

1. `get_clearing_accounts`
2. `get_positions`
3. `get_performance`
4. `get_orders`

Expected checks:

- each call succeeds for the same sandbox account
- empty collections are acceptable if they are valid responses

Do not continue if the account identifier was guessed instead of copied from the previous response.

### Step 6: Optional position history validation

Only do this if the `get_positions` response contains a real `wkn`.

Call:

1. `get_positions_history`

Expected checks:

- use the same account number from Step 5
- use a `wkn` copied from `get_positions`

If no `wkn` is available, skip this step.

### Step 7: Validate market discovery

Only do this if you have a real `wkn` from an earlier response.

Call:

1. `get_trading_venues`

Expected checks:

- use the `wkn` copied from a previous response
- result contains one or more trading venue records, or a valid empty response

Do not call `get_quote` in read-only mode. It is intentionally hidden from the read-only tool set.

## Phase 2: optional full-mode sandbox order validation

Only start this phase if the human explicitly asked for it.

### Step 1: Re-verify environment

Start a full-mode sandbox server, then call:

1. `get_server_info`

Expected checks:

- mode is `full`
- target environment is still sandbox

If either check fails, stop immediately.

### Step 2: Re-run minimum discovery

Call:

1. `get_securities_accounts`
2. `get_positions`
3. `get_trading_venues`

Expected checks:

- all required ids are available from real responses

### Step 3: Quote validation

Only if the previous responses provide enough data, call:

1. `get_quote`

Required inputs:

- `direction`
- `marketPlaceId`
- `nominalAmount`
- `tradingVenueId`
- `wkn` or `isin`

Use only values obtained from the sandbox or provided by the human.

### Step 4: Draft-first order workflow

Only if explicit approval still stands, call:

1. `create_order_entry`
2. `place_order_entry`
3. `poll_order_transaction_state`

Expected pattern:

- draft creation returns a usable order-entry reference
- placement returns a transaction reference or immediate workflow result
- polling reaches a terminal transaction state or times out explicitly

If `create_order_entry` succeeds but approval for placement becomes unclear, stop before `place_order_entry`.

### Step 5: Stop conditions for full mode

Stop immediately if any of these happen:

- the server reports production
- the server reports read-only when full mode was intended
- required ids are missing
- challenge data is requested but not explicitly supplied by the human
- the API returns an auth, permission, or validation error you cannot explain from the previous step

## Minimal validation transcript template

Use this order and do not improvise:

1. `get_server_info`
2. `ping_consorsbank_api`
3. `get_authentication_data`
4. `get_session_level`
5. `get_securities_accounts`
6. `get_clearing_accounts`
7. `get_positions`
8. `get_performance`
9. `get_orders`
10. `get_positions_history` only if `wkn` exists
11. `get_trading_venues` only if `wkn` exists
12. `get_quote` only in full mode and only with approved real inputs
13. `create_order_entry` / `place_order_entry` / `poll_order_transaction_state` only in full mode and only with explicit approval

## What to record after the run

Record these items for the human:

- which OpenClaw agent used the server
- whether the server was in `read-only` or `full`
- whether the target environment was sandbox
- which tools succeeded
- which steps were skipped due to missing ids or lack of approval
- any exact stop condition that ended the run

## Follow-up hardening

This validation path still assumes the local MCP server process can resolve the Consorsbank token. A stricter future design would move the credential into a separate local broker or proxy and keep the MCP server away from the raw token entirely.
