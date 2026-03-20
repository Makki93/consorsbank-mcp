

<div align="center">

<img src="assets/banner.png" alt="Interactive Brokers Web API – Model Context Protocol" />


[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![MseeP.ai Security Assessment Badge](https://mseep.ai/badge.svg)](https://mseep.ai/app/rcontesti-ib-mcp)


![sample](./assets/sample_usecase.gif)

</div>

> [!Note]
>
> #### Project Status
> This project is currently under active development. Features may be incomplete, and breaking changes may occur.

## Motivation 

This project is built exclusively on the Interactive Brokers Web API, and this is a deliberate design choice. While the TWS API is powerful, the Web API provides a more modern and flexible foundation for the goals of this project.

### TWS API MCP vs. WEB API MCP?
**Why not using the TWS API?** The short answer is that the WEB API is planned to be more comprehensive. By more comprehensive I mean, it brings account management, specifically reporting, into the same place. The two other important reasons are: 1. The Web API is standalone. It does not require you to run the TWS desktop software or IB Gateway. 2. Model context protocols (MCPs) are easier to build on top of HTTPS communication rather than TCP/IP sockets.

**If it is more comprehensive why not using just the WEB API?** Three reasons: 1. WEB API is in beta (see [Limitations](#limitations)) 2. TWS is faster and more reliable for trading. 3. TWS has some trading functionalities that the WEB API does not have.

While other projects act as a "bridge" to make the TWS API look like a web API, they introduce an unofficial, third-party dependency. This project avoids that risk by using the official IBKR Web API directly, ensuring better long-term stability and support.


In summary:

* If you need fast and reliable trading go with TWS API
* If you need a more comprehensive and cloud ready solution go with the WEB API


For a more detailed, side-by-side breakdown, please see the [TWS vs. Web API Comparison](#tws-vs-web-comparison) section.


## Table of Contents
- [Table of Contents](#table-of-contents)
- [Overview](#overview)
- [Docker Desktop Setup](#docker-desktop-setup)
  - [Limitations of Multi-Container Setup](#limitations-of-multi-container-setup)
  - [Session Management](#session-management)
- [Future Work](#future-work)
- [Endpoints Status](#endpoints-status)
- [TWS vs WEB comparison](#-tws-vs-web-comparison)
- [Limitations](#limitations)
- [Architecture](#architecture)
  - [📦 Interactive Brokers Client Portal Gateway Docker Container](#-interactive-brokers-client-portal-gateway-docker-container)
      - [🔧 What This Container Does](#-what-this-container-does)
  - [📦 Interactive Brokers Routers Generator Docker Container \[WIP\]](#-interactive-brokers-routers-generator-docker-container-wip)
  - [📦 IB MCP Server Docker Container](#-ib-mcp-server-docker-container)
      - [🔧 What This Container Does](#-what-this-container-does-1)
- [References](#references)
- [Contributing](#contributing)
- [License](#license)

## Overview

This project provides an Interactive Brokers (IB) API interface using the Model Context Protocol (MCP). There are several ways to interact with Interactive Brokers, like the [TWS API](https://www.interactivebrokers.com/campus/ibkr-api-page/twsapi-doc/), the WEB API, [Excel RTD](https://www.interactivebrokers.com/campus/ibkr-api-page/excel-rtd/#introduction) and FIX among others. This project is built on top of Interactive Brokers [WEB API](https://www.interactivebrokers.com/campus/ibkr-api-page/webapi-doc/#introduction-0).

This development uses the retail authentication process which is managed using the Client Portal Gateway, a small Java program used to route local web requests with appropriate authentication. 

## Docker Desktop Setup

See a quick walkthrough in [YOUTUBE](https://www.youtube.com/watch?v=PyQz_kMQ9ek)

1. Clone the repo, set env variables and build the images
    ```bash
    # Clone the repository
    git clone https://github.com/rcontesti/IB_MCP.git

    # Navigate to the project directory
    cd IB_MCP

    # Copy the .env.example file to .env and edit as needed
    cp .env.example .env

    # Build the image
    docker compose up --build -d

    ```
2. Auth with your IB account and credentials to:
    After the image is up and running, navigate to `https://{GATEWAY_BASE_URL}:{GATEWAY_PORT}`⁠ (e.g.: `https://localhost:5055/`) to login.
    You will also find the login path in the logs of the API gateway container:
    ![Where to find the login path](./assets/LOGIN_URL.png)
    If successful you should be redirected to a URL that reads: "Client login succeeds".

3. Add the MCP server config file to your VS Code `settings.json`.

    Given the following environment parameters
    ```
    MCP_SERVER_HOST=0.0.0.0
    MCP_SERVER_PORT=5002
    MCP_SERVER_PATH=/mcp
    MCP_TRANSPORT_PROTOCOL=streamable-http
    ```

    the VS Code MCP server snippet in `settings.json` would look like:

    ```json
    {
      ...
        },
        "chat.mcp.discovery.enabled": true,
        "mcp": {
            "inputs": [],
            "servers": {
                "time": {
                "command": "docker",
                "args": ["run", "-i", "--rm", "mcp/time"]
                },
                "ib-web": {
                    "type": "http",
                    "url": "http://localhost:5002/mcp/",
                }
            }
        },
        "workbench.colorTheme": "Tomorrow Night Blue"
    }
    ```
    Alternatively, you can create a `.vscode/mcp.json` file at the root of your project with the following content:

    ```json
    {
        "servers": {
            "ib-mcp-server": {
                "type": "http",
                "url": "http://localhost:5002/mcp/"
            }
        },
        "inputs": []
    }
    ```
    Check [Use MCP servers in VS Code (Preview)](https://code.visualstudio.com/docs/copilot/chat/mcp-servers) for further reference.

4. Start the MCP in Copilot



### Limitations of Multi-Container Setup

- Users must log in through the browser on the same machine as Client Portal Gateway in order to authenticate.
- All API Endpoint calls must be made on the same machine where the Client Portal Gateway was authenticated.
- None of the endpoints beginning with /gw/api, /oauth, or /oauth2 are supported for use in the Client Portal Gateway.

### Session Management

The additional /iserver/auth/ssodh/init endpoint is used to subsequently reopen a brokerage session with the backend, through which you can access the protected /iserver endpoints.

Sessions will time out after approximately 6 minutes without sending new requests or maintaining the /tickle endpoint at least every 5 minutes.

In order to prevent the session from timing out, the endpoint /tickle should be called on a regular basis. It is recommended to call this endpoint approximately every minute.

If the brokerage session has timed out but the session is still connected to the IBKR backend, the response to /auth/status returns ‘connected’:true and ‘authenticated’:false. Calling the /iserver/auth/ssodh/init endpoint will initialize a new brokerage session.

## Future Work
- Automatically generate endpoints
  - Currently the [IB REST API (2.16.0) OpenAPI specification](https://api.ibkr.com/gw/api/v3/api-docs) fails validation, and the automated router generation feature is currently failing to generate routers. You can try to validate yourself here:

  - https://oas-validation.com/
  - https://editor.swagger.io/

  The spec currently has 351 errors. Therefore, router endpoints are currently being built manually, and their status is updated upon completion.
  - Due to issues with the official OpenAPI specification and the IB team's current focus, automated router generation is not feasible at this time, and routers are being built manually.
- Add OAuth

## Endpoints Status

Endpoints are currently manually built.

👉 See the full list of [API Endpoints Staus](ENDPOINTS.md)


## TWS vs WEB comparison


*   **TWS API (Trader Workstation API):** The legacy, powerhouse API. It's extremely powerful, fast, and feature-rich, but it's also more complex and **requires you to run the TWS desktop software or the IB Gateway** on a machine continuously.
*   **Web API (Client Portal API):** The modern, developer-friendly RESTful API. It's much easier to use, works over standard HTTPS, and is standalone (no desktop software needed). However, it is less performant and has fewer features than the TWS API.

### Comparison Table

| Feature              | TWS API                                                                                      | Web API (Client Portal API)                                                                      |
| :------------------- | :------------------------------------------------------------------------------------------- | :----------------------------------------------------------------------------------------------- |
| **Technology**       | TCP/IP Socket, Proprietary binary protocol                                                   | RESTful, HTTPS, JSON format                                                                      |
| **Dependency**       | **Requires TWS or IB Gateway to be running**                                                 | **Standalone** (does not require TWS/Gateway)                                                    |
| **Ease of Use**      | Steep learning curve, complex                                                                | Much easier, standard for web developers                                                         |
| **Performance**      | **Very high speed, low latency.** Ideal for high-frequency data and fast order execution.    | Slower due to HTTPS overhead. Not for HFT.                                                       |
| **Functionality**    | **Extremely comprehensive.** Access to virtually every feature in TWS, including complex order types, combos, algos, etc. | More limited. Covers core trading, portfolio, and market data, but lacks the most advanced features. |
| **Data Streaming**   | Robust, high-frequency streaming for market data, account updates, etc.                      | Supports streaming via a WebSocket connection (`/ws`).                                           |
| **Historical Data**  | **Very deep and extensive access** to historical data.                                       | Good access, but can be more limited in scope and request frequency.                             |
| **Authentication**   | Connects directly to an authenticated TWS/Gateway session.                                   | Modern OAuth-based authentication. More secure for web apps but requires a more complex initial setup. |
| **Ideal Use Cases**  | Algorithmic trading, custom trading desktops, high-frequency strategies, complex options analysis. | Web dashboards, mobile apps, portfolio analysis tools, simple trading bots, reporting.             |
| **Community/Libraries**| Very mature. Many third-party libraries in Python, Java, C#, C++, etc.                       | Newer, but growing. Easy to use with any language that can make HTTP requests.                   |

### TWS API: Detailed Pros and Cons

#### ✅ Pros of the TWS API

1.  **Unmatched Functionality:** This is its biggest advantage. You can do almost anything the TWS desktop platform can do, including creating complex multi-leg combo orders (options strategies), accessing dozens of algorithmic order types (VWAP, TWAP), and managing allocations for financial advisors.
2.  **High Performance and Low Latency:** By using a direct socket connection, the TWS API is incredibly fast. It's built for speed and is the only choice for strategies that are sensitive to latency. Market data streaming is real-time and efficient.
3.  **Extensive Historical Data:** It provides deep access to historical data, allowing for extensive backtesting and analysis directly through the API.
4.  **Maturity and Robustness:** The API has been around for decades. It's well-tested, stable, and has a large community with a wealth of examples and third-party wrappers in many programming languages.

#### ❌ Cons of the TWS API

1.  **The TWS/Gateway Dependency:** This is the single biggest drawback. Your application is not standalone. It requires an instance of the Trader Workstation or the more lightweight IB Gateway to be running and logged in on a server or computer 24/7. This adds operational complexity, a point of failure, and resource overhead.
2.  **Steep Learning Curve:** The API is not intuitive. It uses a proprietary request/response model with message IDs and a specific sequence of calls. Error handling can be tricky, and debugging requires understanding its unique architecture, not standard web protocols.
3.  **Complexity:** The protocol is binary and requires a client library (like IB's official ones or a third-party wrapper) to interact with. You aren't just sending simple JSON payloads.

### Web API (Client Portal API): Detailed Pros and Cons

#### ✅ Pros of the Web API

1.  **Ease of Use and Modern Standards:** It's a standard RESTful API. If you've ever worked with a web API, you'll feel right at home. You make HTTPS requests and get back clean, human-readable JSON. This dramatically lowers the barrier to entry.
2.  **No Desktop Dependency:** This is its killer feature. Your application can run anywhere (e.g., a cloud server, a serverless function) without needing to maintain a separate TWS/Gateway process. This vastly simplifies deployment and maintenance.
3.  **Great for Web and Mobile:** Because it's a standard web API, it's perfect for building web-based dashboards, mobile applications, or integrating IBKR data into other web services.
4.  **Secure Authentication:** It uses OAuth, the industry standard for secure, delegated access, which is ideal for applications where a user grants your app permission to access their account.

#### ❌ Cons of the Web API

1.  **Limited Functionality:** It does not expose the full range of features available in TWS. You can place standard orders (Market, Limit, Stop), but you won't find the complex algorithmic or multi-leg combo orders that the TWS API supports.
2.  **Lower Performance:** The overhead of HTTPS/JSON makes it inherently slower than the direct socket connection of the TWS API. It is not suitable for high-frequency or latency-sensitive trading strategies.
3.  **Rate Limiting:** As a web-based service, it is subject to more explicit rate limits on requests to ensure fair usage for all clients.
4.  **Newer and Less Mature:** While now quite stable, it's newer than the TWS API. You might occasionally find a feature is missing or that the documentation isn't as battle-hardened as the decades-old TWS API.

### Which One Should You Choose?

**Choose the TWS API if:**

*   You are building a serious, automated **algorithmic trading system**.
*   Your strategy is **latency-sensitive**.
*   You need to place **complex order types** like options spreads, combos, or use IBKR's built-in algos.
*   You are building a custom, feature-rich **desktop trading application**.
*   You are comfortable with the operational overhead of running the **IB Gateway 24/7**.

**Choose the Web API (Client Portal API) if:**

*   You are building a **web dashboard** or **mobile app** to monitor your portfolio.
*   You want to build a **simple trading bot** for executing basic orders (e.g., "buy 100 shares of AAPL when it hits $150").
*   You are building a **reporting or analytics tool**.
*   **Development speed and ease of use** are more important than raw performance or access to every single feature.
*   You **cannot or do not want to run a dedicated TWS/Gateway instance**.

## Architecture

The project consists of 2 main Docker services:

*   **api_gateway:** Runs the Interactive Brokers Client Portal Gateway to enable secure access to the IB REST API. Includes an integrated tickler service that automatically maintains the IB session by periodically calling the `/tickle` endpoint to prevent timeouts.
*   **mcp_server:** FastMCP server built with FastAPI that provides the Model Context Protocol interface. Contains manually-developed routers for IB API endpoints located in `mcp_server/routers/`.


### 📦 Interactive Brokers Client Portal Gateway Docker Container

This Docker container sets up and runs the **Interactive Brokers (IB) Client Portal Gateway**, which is required for applications to connect via the IB REST API.

##### 🔧 What This Container Does

- **Base Image**: Uses `eclipse-temurin:21` (Java 21) for compatibility with the IB Gateway.
- **Installs Dependencies**: Installs `unzip` for extracting the gateway archive, and `curl` for the tickler service.
- **Downloads Gateway**: Fetches the latest version of the Client Portal Gateway from the official Interactive Brokers source and unzips it.
- **Configuration**:
  - Copies a custom `conf.yaml` into the expected path (`gateway/root/conf.yaml`) to configure the gateway.
  - Includes a `run_gateway.sh` script that starts the gateway and launches the integrated tickler service.
  - The tickler automatically calls the `/tickle` endpoint every 60 seconds to maintain the session.
- **Port Exposure**: Exposes port `5055` (default port used by the gateway). Override as needed in .env.
- **Startup Command**: Runs the gateway and tickler service using the configuration file.

This setup provides a self-contained, reproducible environment for securely running the Interactive Brokers REST API gateway with automatic session management in a containerized environment.

### 📦 IB MCP Server Docker Container
This Docker container sets up and runs the **Interactive Brokers (IB) Model Context Protocol (MCP) Server**, which provides an interface for interacting with the IB API gateway.

##### 🔧 What This Container Does

- **Base Image**: Uses `ghcr.io/astral-sh/uv:python3.11-bookworm-slim` for a lightweight Python 3.11 environment with `uv`.
- **Installs Dependencies**: Installs `curl` for system dependencies and uses `uv sync` to install Python dependencies from `pyproject.toml`.
- **Configuration**:
  - Copies the `pyproject.toml` and the entire `mcp_server` directory (including `mcp_server/routers/`) into the container.
  - Sets `PYTHONPATH` to `/app` and `UV_CACHE_DIR` to `/tmp/uv-cache`.
  - Routers are manually developed and located in `mcp_server/routers/` (not auto-generated due to OpenAPI spec validation issues).
- **Port Exposure**: Exposes the port specified by the `MCP_SERVER_PORT` environment variable (e.g., `5002`).
- **Startup Command**: Runs the FastAPI server using `uv run -- python /app/mcp_server/fastapi_server.py`.

This setup provides a containerized environment for the MCP server with integrated routers, enabling it to communicate with the IB Client Portal Gateway via the Model Context Protocol.

## Limitations

- **Claude Desktop:** The current development only supports streamable HTTP and claude desktop Remote MCP server support is currently in beta and available for users on Claude Pro, Max, Team, and Enterprise plans (as of June 2025).
- **Cline:** Cline still has issues with streamable HTTP with remote servers.

## References
- [IB WEB API Reference](https://www.interactivebrokers.com/campus/ibkr-api-page/webapi-ref/)
- [IB WEB API openapi docs](https://api.ibkr.com/gw/api/v3/api-docs) Outdated!
- [IB WEB API Reference page](https://www.interactivebrokers.com/campus/ibkr-api-page/cpapi-v1/#introduction) 
  
- [FAST MCP](https://github.com/jlowin/fastmcp)
- [FAST MCP Documentation](https://gofastmcp.com/servers/fastmcp)
- [FAST MCP openapi integration](https://gofastmcp.com/servers/openapi)

- [ibeam](https://github.com/Voyz/ibeam)
- [fastapi-codegen](https://github.com/koxudaxi/fastapi-code-generator)
- [openapi spec validator repo](https://github.com/python-openapi/openapi-spec-validator)
- [openapi spec validator docs](https://openapi-spec-validator.readthedocs.io/en/latest/python.html)

## Contributing
We welcome contributions to this project! If you'd like to contribute, please follow these guidelines:

1.  **Fork the repository** and create your branch from `main`.
2.  **Report bugs** by opening an issue with a clear description and steps to reproduce.
3.  **Suggest features** by opening an issue to discuss your ideas.
4.  **Submit pull requests** for bug fixes, new features, or improvements. Please ensure your code adheres to the existing style, includes relevant tests, and has clear commit messages.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
