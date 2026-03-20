# Consorsbank API Integration Guide

## Overview

This document describes the Consorsbank Trading API: endpoints, authentication, and how it compares to the comdirect API used elsewhere in the project.

## API Endpoint Mapping

### Base URLs and paths

- Base URLs:
  - Production: https://api.consorsbank.de/trading
  - Sandbox: https://api.consorsbank.de/sandbox/trading
- Auth:
  - Session TAN: `/v1/profile/session-tan`
  - Session trust level: `/v1/profile/session-level`
- Accounts & portfolios:
  - Securities accounts: `/v1/securities-accounts`
  - Clearing accounts of a securities account: `/v1/securities-accounts/{no}/accounts`
  - Positions: `/v1/securities-accounts/{no}/positions`
  - Performance: `/v1/securities-accounts/{no}/performance`
- Trading venues:
  - `/v1/securities/{wkn}/tradingvenues`
- Quotes & trading:
  - Quotes: `POST /v1/quotes`
  - Quote order entries: `/v1/quote-order-entries`
  - Order entries (create/update/place): `/v1/order-entries`
  - Order changes: `/v1/order-changes`
  - Cancel order: `POST /v1/orders/{no}/cancel`
  - Ex-ante cost: `GET /v1/ex-ante-costs/{id}`

### Authentication Flow

| **Capability**     | **Comdirect**                       | **Consorsbank**                    |
| ------------------ | ----------------------------------- | ---------------------------------- |
| OAuth2 Initiation  | `POST /api/comdirect/auth/initiate` | OAuth 2.0 Authorization Code flow |
| TAN Challenge      | Integrated with OAuth flow          | Session TAN for transactions       |
| TAN Completion     | `POST /api/comdirect/auth/complete` | TAN validation via API             |
| Session Management | Full session lifecycle              | Bearer token + refresh             |
| Auth Status        | `GET /api/comdirect/auth/status`    | Session validation                 |

### Account & Portfolio Data

| **Endpoint**          | **Comdirect API**                                  | **Consorsbank API**                         |
| --------------------- | -------------------------------------------------- | ------------------------------------------- |
| **Account Balances**  | `/banking/clients/user/v2/accounts/balances`       | `/v1/accounts`                              |
| **Securities Accounts** | Included in accounts                            | `/v1/securities-accounts`                   |
| **Depot Positions**   | `/brokerage/v3/depots/{id}/positions`              | `/v1/securities-accounts/{id}/positions`    |
| **Performance Data**  | Separate endpoint                                  | `/v1/securities-accounts/{id}/performance`  |
| **Portfolio Sync**    | Custom implementation                              | Composite of the accounts/portfolio APIs   |

### Market Data & Instruments

| **Endpoint**          | **Comdirect API**                    | **Consorsbank API**                          |
| --------------------- | ------------------------------------ | -------------------------------------------- |
| **Instrument Search** | `/brokerage/v1/instruments/search` | No direct equivalent (use order flow)        |
| **Quotes**            | Custom implementation                | `POST /v1/quotes`                            |
| **Trading Venues**    | —                                    | `/v1/trading-venues`                         |

### Order Management

| **Endpoint**        | **Comdirect API**                              | **Consorsbank API**                                              |
| ------------------- | ---------------------------------------------- | ---------------------------------------------------------------- |
| **Cost Estimation** | `/brokerage/v3/orders/costindicationexante`    | `GET /v1/ex-ante-costs/{id}`                                     |
| **Order Preview**   | Custom cost estimation                         | Create order entry + estimate                                    |
| **Place Order**     | `/brokerage/v3/orders`                         | `POST /v1/order-entries` → `POST /v1/order-entries/{id}`         |
| **Cancel Order**    | `/brokerage/v1/orders/{id}/cancel`             | `POST /v1/orders/{no}/cancel`                  |
| **Order Status**    | `/brokerage/depots/{depotId}/v3/orders`        | `/v1/orders` (filtered)                                          |

### Advanced Features

| **Feature**           | **Comdirect**   | **Consorsbank API**                 |
| --------------------- | --------------- | ----------------------------------- |
| **Savings Plans**     | —               | `/v1/savings-plans*` endpoints      |
| **One-Time Payments** | —               | `/v1/one-time-payments*` endpoints |
| **Quote Orders**      | —               | `/v1/quote-order-entries`          |

## Authentication Architecture

### OAuth 2.0 Flow Implementation

```kotlin
// Required for Consorsbank integration
class ConsorsAuthenticator {
    // 1. Authorization Code Request
    suspend fun initiateOAuth(): String {
        // Redirect to: https://identity.consorsbank.de/oauth/authorize
        // with client_id, response_type=code, redirect_uri
    }

    // 2. Token Exchange
    suspend fun exchangeCodeForToken(code: String): ConsorsTokenResponse {
        // POST https://identity.consorsbank.de/oauth/token
        // with grant_type=authorization_code, code, client_id, client_secret
    }

    // 3. Session TAN for Trading
    suspend fun enableSessionTan(token: String): ConsorsTanChallenge {
        // POST /v1/session-tan with Bearer token
    }

    // 4. Token Refresh
    suspend fun refreshToken(refreshToken: String): ConsorsTokenResponse {
        // POST /oauth/token with grant_type=refresh_token
    }
}
```

### Required Environment Variables

```bash
# Consorsbank OAuth Configuration
CONSORS_CLIENT_ID="your_client_id"
CONSORS_CLIENT_SECRET="your_client_secret"
CONSORS_REDIRECT_URI="https://yourapp.com/api/auth/consors/callback"

# API Configuration
CONSORS_SANDBOX=true
CONSORS_BASE_URL="https://api.consorsbank.de/v1"
CONSORS_OAUTH_URL="https://identity.consorsbank.de/oauth"

# Rate Limiting
CONSORS_RATE_LIMIT_RPM=60
CONSORS_RATE_LIMIT_BURST=10

# Session Management
CONSORS_SESSION_REDIS_ENABLED=false
CONSORS_TOKEN_REFRESH_BUFFER_MINUTES=5
```

## Service layer patterns

Following comdirect patterns for consistency:

```kotlin
// Service Layer (mirrors ComdirectService)
@Service
class ConsorsService(
    private val apiClient: ConsorsApiClient,
    private val auditLogger: ConsorsAuditLogger,
    private val rateLimiter: ConsorsRateLimiter,
    private val cacheManager: ConsorsCacheManager
) {
    @Cacheable(value = [SECURITIES_ACCOUNTS_CACHE], key = "'accounts:' + userId")
    fun getSecuritiesAccounts(): List<ConsorsSecuritiesAccount> {
        val userId = getCurrentUserId()
        rateLimiter.checkRateLimit(userId)
        auditLogger.logDataAccess(SECURITIES_ACCOUNTS_ACCESS)
        return apiClient.getSecuritiesAccounts()
    }
}

// API Client (mirrors ComdirectApiClient)
@Component
class ConsorsApiClient(
    private val authenticator: ConsorsAuthenticator,
    private val config: ConsorsConfig,
    webClientBuilder: WebClient.Builder
) {
    private val webClient = webClientBuilder.baseUrl(config.getEffectiveBaseUrl()).build()

    suspend fun getSecuritiesAccounts(): List<ConsorsSecuritiesAccount> {
        val session = getAuthenticatedSession()
        return webClient.get()
            .uri("/securities-accounts")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${session.accessToken}")
            .retrieve()
            .awaitBody()
    }
}
```

## Possible implementation strategies for Consorsbank API

### Cache Strategy

```yaml
# Consorsbank Cache Configuration
caching:
  consors:
    securities-accounts:
      ttl: 300s # 5 minutes
    positions:
      ttl: 180s # 3 minutes
    quotes:
      ttl: 30s # 30 seconds
    instruments:
      ttl: 3600s # 1 hour
    orders:
      ttl: 60s # 1 minute
```

### Rate Limiting & Reliability

- Default target: **60 requests/minute** with burst handling (align `CONSORS_RATE_LIMIT_*` with Consorsbank’s published limits).
- Use token-bucket style limiting and backoff on transient errors; mirror the approach used for comdirect where applicable.

### Error Handling

- Map Consorsbank error responses to a small internal taxonomy and apply retries with exponential backoff for idempotent reads and safe operations.

### Session Management

- Persist tokens and session metadata as needed (e.g. Redis) with refresh before expiry; keep refresh buffer configurable via `CONSORS_TOKEN_REFRESH_BUFFER_MINUTES`.

### Audit & Compliance

- Log security-relevant API access (similar to comdirect audit patterns) for troubleshooting and compliance.
