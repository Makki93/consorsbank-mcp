import os
import sys

# Add routers path
# Load routers path and inject into sys.path
ROUTERS_PATH = os.environ.get("ROUTERS_PATH")
if os.path.isdir(ROUTERS_PATH):
    sys.path.insert(0, ROUTERS_PATH)
else:
    print(f"⚠️ Warning: ROUTERS_PATH '{ROUTERS_PATH}' does not exist.")


# Load environment variables
GATEWAY_PORT = os.environ.get("GATEWAY_PORT")
GATEWAY_ENDPOINT = os.environ.get("GATEWAY_ENDPOINT")
GATEWAY_INTERNAL_BASE_URL = os.environ.get("GATEWAY_INTERNAL_BASE_URL")

MCP_SERVER_BASE_URL = os.environ.get("MCP_SERVER_BASE_URL")
MCP_SERVER_INTERNAL_BASE_URL = os.environ.get("MCP_SERVER_INTERNAL_BASE_URL")
MCP_SERVER_HOST = os.environ.get("MCP_SERVER_HOST")
MCP_TRANSPORT_PROTOCOL = os.environ.get("MCP_TRANSPORT_PROTOCOL")
MCP_SERVER_PORT = os.environ.get("MCP_SERVER_PORT")

INCLUDED_TAGS = os.getenv("INCLUDED_TAGS")
EXCLUDED_TAGS = os.getenv("EXCLUDED_TAGS")
print(EXCLUDED_TAGS)

# Add validation and type conversion for MCP_SERVER_PORT
if not MCP_SERVER_PORT:
    print("Error: MCP_SERVER_PORT environment variable is not set.")
    sys.exit(1)
try:
    MCP_SERVER_PORT = int(MCP_SERVER_PORT)
except ValueError:
    print("Error: MCP_SERVER_PORT must be a valid integer.")
    sys.exit(1)


BASE_URL = f"{GATEWAY_INTERNAL_BASE_URL}:{GATEWAY_PORT}{GATEWAY_ENDPOINT}"
print("BASE_URL:", BASE_URL)

# Create FastAPI object description based on filters
base_description = """
A comprehensive FastAPI wrapper for the Interactive Brokers Web API. 
This API provides a clean, modern interface to interact with various IBKR functionalities.
"""

# --- Configuration ---
# Define all possible modules and their descriptions in a dictionary for easy management.
ALL_MODULES = {
    "Alerts": "Create, modify, delete, and monitor price, time, and margin alerts.",
    "Contract": "Search for and retrieve detailed information on financial instruments including stocks, options, futures, and bonds.",
    "Events Contracts": "Get details on contracts that settle based on the outcome of future events.",
    "FA Allocation Management": "Manage Financial Advisor allocation groups for trade distribution.",
    "FYIs & Notifications": "Manage and retrieve notifications, disclaimers, and delivery options.",
    "Market Data": "Access live and historical market data, including snapshots, history, and deep history from HMDS.",
    "Options Chains": "Retrieve full option chains for underlying symbols.",
    "Order Monitoring": "Check the status of live orders and view a list of recent trades.",
    "Orders": "Place, preview, modify, and cancel trading orders.",
    "Portfolio": "Get detailed information about account portfolios, including positions, allocation, summaries, and performance.",
    "Portfolio Analyst": "Access performance data and transaction history for accounts.",
    "Scanner": "Run market scanners on both iServer and the Historical Market Data Service (HMDS).",
    "Session": "Manage the user's authentication session, including status checks, re-authentication, and logout.",
    "Watchlists": "Create, delete, and manage watchlists and the contracts within them."
}



# Start with an initial set of modules.
if INCLUDED_TAGS:
    # If INCLUDED_TAGS is set, it defines the base set.
    cleaned_included_str = INCLUDED_TAGS.replace('\n', '').replace('"', '')
    INCLUDED_TAGS_SET = {tag.strip() for tag in cleaned_included_str.split(',') if tag.strip()}
    display_modules = {tag: desc for tag, desc in ALL_MODULES.items() if tag in INCLUDED_TAGS_SET}
else:
    # Otherwise, the base set is all modules.
    display_modules = ALL_MODULES.copy()

# Now, filter out any excluded tags from the base set.
if EXCLUDED_TAGS:
    cleaned_excluded_str = EXCLUDED_TAGS.replace('\n', '').replace('"', '')
    EXCLUDED_TAGS_SET = {tag.strip() for tag in cleaned_excluded_str.split(',') if tag.strip()}
    display_modules = {tag: desc for tag, desc in display_modules.items() if tag not in EXCLUDED_TAGS_SET}
else:
    EXCLUDED_TAGS_SET = {}

# Dynamically build the list of available modules.
module_list_str = "\n**Available Modules:**\n\n"
# Sort the items alphabetically for consistent output.
for name, desc in sorted(display_modules.items()):
    module_list_str += f"* **{name}**: {desc}\n"

# Combine the base description with the dynamic module list.
FINAL_DESCRIPTION = base_description + module_list_str
