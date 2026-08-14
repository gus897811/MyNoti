from fastapi import Header

from app.config import settings
from app.errors import APIError


async def verify_api_key(x_api_key: str = Header(default=None)):
    """모든 요청의 X-API-Key 헤더를 검증합니다."""
    if not x_api_key or x_api_key != settings.API_KEY:
        raise APIError(401, "UNAUTHORIZED", "API 키가 없거나 올바르지 않습니다.")
    return True
