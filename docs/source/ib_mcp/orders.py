# orders.py
from fastapi import APIRouter, Query, Body, Path
from typing import Optional, List, Dict, Any
import httpx
from pydantic import BaseModel, Field
from mcp_server.config import BASE_URL

router = APIRouter()

# --- Pydantic Models for Order Requests ---

class OrderModel(BaseModel):
    """
    Pydantic model representing a single order for placement or modification.
    Based on the structure provided in the IBKR Web API documentation.
    """
    acctId: Optional[str] = Field(None, description="Account ID for the order. Should match the accountId in the path.")
    conid: Optional[int] = Field(None, description="Contract ID for the security. Use either conid or conidex.")
    conidex: Optional[str] = Field(None, description="Contract ID with exchange, e.g., '265598@SMART'. Use for direct routing.")
    secType: Optional[str] = Field(None, description="Security type, e.g., '265598:STK'.")
    cOID: Optional[str] = Field(None, description="Customer-specific order ID. Must be unique for 24 hours.")
    parentId: Optional[str] = Field(None, description="Parent order ID for child orders in bracket or OCA groups.")
    orderType: str = Field(..., description="The type of order, e.g., LMT, MKT, STP.")
    listingExchange: Optional[str] = Field(None, description="Primary routing exchange. Defaults to SMART.")
    outsideRTH: Optional[bool] = Field(False, description="Set to true to allow execution outside regular trading hours.")
    price: Optional[float] = Field(None, description="The limit price for LMT orders, or the stop price for STP orders.")
    auxPrice: Optional[float] = Field(None, description="The auxiliary price, used for STOP_LIMIT and TRAILLMT orders.")
    side: str = Field(..., description="The side of the order: BUY or SELL.")
    ticker: Optional[str] = Field(None, description="The ticker symbol for the contract.")
    tif: str = Field(..., description="The time in force for the order, e.g., GTC, DAY, IOC.")
    quantity: float = Field(..., description="The number of shares or contracts to trade.")
    useAdaptive: Optional[bool] = Field(False, description="Set to true to use the Price Management Algo.")
    strategy: Optional[str] = Field(None, description="The IB Algo strategy to use.")
    strategyParameters: Optional[Dict[str, Any]] = Field(None, description="A dictionary of parameters for the specified IB Algo strategy.")


class OrdersRequest(BaseModel):
    """Request model for placing one or more orders."""
    orders: List[OrderModel]

class ReplyRequest(BaseModel):
    """Request model for confirming an order with a reply ID."""
    confirmed: bool = Field(..., description="Set to true to confirm and submit the order.")


# --- Orders Router Endpoints ---

@router.post(
    "/iserver/account/{accountId}/orders",
    tags=["Orders"],
    summary="Place Order(s)",
    description="Place one or more orders. For bracket or OCA orders, use the cOID of the parent in the parentId field of the child orders."
)
async def place_order(
    accountId: str = Path(..., description="The account ID to place the order for."),
    body: OrdersRequest = Body(...)
):
    """
    Places one or more orders for the specified account.
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.post(
                f"{BASE_URL}/iserver/account/{accountId}/orders",
                json=body.dict(exclude_none=True),
                timeout=10
            )
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}


@router.post(
    "/iserver/account/{accountId}/orders/whatif",
    tags=["Orders"],
    summary="Preview Order",
    description="Preview an order without submitting it to get commission and margin impact information."
)
async def preview_order(
    accountId: str = Path(..., description="The account ID for the what-if analysis."),
    body: OrdersRequest = Body(...)
):
    """
    Previews an order to see its potential impact on the account before placing it.
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.post(
                f"{BASE_URL}/iserver/account/{accountId}/orders/whatif",
                json=body.dict(exclude_none=True),
                timeout=10
            )
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}


@router.post(
    "/iserver/account/{accountId}/order/{orderId}",
    tags=["Orders"],
    summary="Modify Order",
    description="Modifies an existing open order."
)
async def modify_order(
    accountId: str = Path(..., description="The account ID of the order."),
    orderId: str = Path(..., description="The order ID of the order to modify."),
    body: OrderModel = Body(...)
):
    """
    Modifies an existing active order. The request body should contain the updated order details.
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.post(
                f"{BASE_URL}/iserver/account/{accountId}/order/{orderId}",
                json=body.dict(exclude_none=True),
                timeout=10
            )
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}


@router.delete(
    "/iserver/account/{accountId}/order/{orderId}",
    tags=["Orders"],
    summary="Cancel Order",
    description="Cancels an open order."
)
async def cancel_order(
    accountId: str = Path(..., description="The account ID of the order."),
    orderId: str = Path(..., description="The order ID of the order to cancel.")
):
    """
    Cancels an active order by its ID.
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.delete(
                f"{BASE_URL}/iserver/account/{accountId}/order/{orderId}",
                timeout=10
            )
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}


@router.post(
    "/iserver/reply/{replyId}",
    tags=["Orders"],
    summary="Place Order Reply",
    description="Reply to a confirmation message received after attempting to place an order."
)
async def place_order_reply(
    replyId: str = Path(..., description="The ID of the message to reply to."),
    body: ReplyRequest = Body(...)
):
    """
    Confirms an order that requires a secondary confirmation (e.g., due to price or size constraints).
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.post(
                f"{BASE_URL}/iserver/reply/{replyId}",
                json=body.dict(),
                timeout=10
            )
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}
