import os
from fastapi import FastAPI
from fastmcp import FastMCP
from fastmcp.server.openapi import RouteMap, MCPType
from mcp_server.config import MCP_SERVER_HOST, MCP_SERVER_PORT, MCP_TRANSPORT_PROTOCOL, FINAL_DESCRIPTION, EXCLUDED_TAGS_SET

# Import Router Files
import alerts
import contract
import events_contracts
import fa_allocation_management
import fyis_and_notifications
import market_data
import options_chains
import order_monitoring
import orders
import portfolio
import scanner
import session
import watchlists


app = FastAPI(
    title="IBKR API",
    description=FINAL_DESCRIPTION,
    version="1.0.0"
)

app.include_router(alerts.router)
app.include_router(contract.router)
app.include_router(events_contracts.router)
app.include_router(fa_allocation_management.router)
app.include_router(fyis_and_notifications.router)
app.include_router(market_data.router)
app.include_router(options_chains.router)
app.include_router(order_monitoring.router)
app.include_router(orders.router)
app.include_router(portfolio.router)
app.include_router(scanner.router)
app.include_router(session.router)
app.include_router(watchlists.router)


route_maps_list = []

if EXCLUDED_TAGS_SET:    
    for tag_ in EXCLUDED_TAGS_SET:
        route_maps_list.append(RouteMap(tags={tag_}, mcp_type=MCPType.EXCLUDE))


mcp = FastMCP.from_fastapi(
    app=app,
    route_maps = route_maps_list,
    )

if __name__ == "__main__":
    mcp.run(
        transport=MCP_TRANSPORT_PROTOCOL,
        host=MCP_SERVER_HOST,
        port=MCP_SERVER_PORT,
        log_level="DEBUG",
    )
