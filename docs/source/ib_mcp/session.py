# session.py
from fastapi import APIRouter
import httpx
from mcp_server.config import BASE_URL

router = APIRouter()

# --- Session Router Endpoints ---

@router.post(
    "/sso/validate",
    tags=["Session"],
    summary="Validate SSO",
    description="Validates the current session for the SSO user."
)
async def sso_validate():
    """
    Validates the session for a Single Sign-On (SSO) user.
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.post(f"{BASE_URL}/sso/validate", timeout=10)
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}

@router.get(
    "/iserver/auth/status",
    tags=["Session"],
    summary="Authentication Status",
    description="Returns the authentication status of the gateway."
)
async def get_auth_status():
    """
    Checks the current authentication status, including connection status, any competing sessions, and server info.
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.get(f"{BASE_URL}/iserver/auth/status", timeout=10)
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}

@router.post(
    "/iserver/reauthenticate",
    tags=["Session"],
    summary="Re-authenticate",
    description="Attempts to re-authenticate a session that has expired."
)
async def reauthenticate():
    """
    When the session has been idle for a long time, it may expire. This endpoint can be used to re-authenticate the session.
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.post(f"{BASE_URL}/iserver/reauthenticate", timeout=10)
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}

@router.post(
    "/logout",
    tags=["Session"],
    summary="Terminate Session",
    description="Logs the user out of the gateway session."
)
async def logout():
    """
    Terminates the current brokerage session.
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.post(f"{BASE_URL}/logout", timeout=10)
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}

@router.get(
    "/tickle",
    tags=["Session"],
    summary="Tickle",
    description="Keeps the session open and verifies that the gateway is running. If the gateway is not running, it will not respond."
)
async def tickle():
    """
    Pings the gateway to keep the session alive and check for connectivity.
    """
    async with httpx.AsyncClient(verify=False) as client:
        try:
            response = await client.get(f"{BASE_URL}/tickle", timeout=10)
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            return {"error": "IBKR API Error", "status_code": exc.response.status_code, "detail": exc.response.text}
        except httpx.RequestError as exc:
            return {"error": "Request Error", "detail": str(exc)}
