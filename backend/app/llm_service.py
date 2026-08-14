import json
from typing import Optional

import openai
from openai import AsyncOpenAI

from app.config import settings
from app.prompts import get_system_prompt

_client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)

# analyze/analyze-batch 응답을 강제하는 JSON 스키마.
# strict: true + additionalProperties: false 로 스키마를 벗어난 응답을 원천 차단합니다.
# OpenAI Structured Outputs 제약상 모든 필드가 required에 들어가야 하며,
# 값이 없을 수 있는 필드(deadline)는 optional 대신 nullable 타입으로 표현합니다.
STRUCTURE_SCHEMA = {
    "name": "analyze_notification",
    "strict": True,
    "schema": {
        "type": "object",
        "properties": {
            "summary": {"type": "string"},
            "title": {"type": "string"},
            "isImportant": {"type": "boolean"},
            "type": {
                "type": "string",
                "enum": ["CLASS", "ASSIGNMENT", "COMMUNICATION", "FINANCIAL", "ETC"],
            },
            "actionRequired": {"type": "boolean"},
            "deadline": {"type": ["string", "null"]},
            "actions": {"type": "array", "items": {"type": "string"}},
        },
        "required": [
            "summary",
            "title",
            "isImportant",
            "type",
            "actionRequired",
            "deadline",
            "actions",
        ],
        "additionalProperties": False,
    },
}


class LLMTimeoutError(Exception):
    pass


class LLMCallError(Exception):
    pass


def fallback_result(title: Optional[str], content: Optional[str]) -> dict:
    """LLM 호출 실패/타임아웃 시 사용하는 규칙 기반 폴백 결과."""
    combined = f"{title or ''} {content or ''}".strip()
    return {
        "summary": combined[:50] if combined else "새 알림이 도착했습니다.",
        "title": (title or "").strip() or "새 알림",
        "isImportant": False,
        "type": "ETC",
        "actionRequired": False,
        "deadline": None,
        "actions": [],
    }


async def analyze_notification(
    app_name: str,
    package_name: str,
    title: Optional[str],
    content: Optional[str],
    received_at: str,
) -> dict:
    """알림 원문을 LLM에 보내 구조화된 분석 결과(dict)를 받아옵니다.

    백엔드는 이 결과를 저장하지 않고 그대로 안드로이드에 반환합니다 (Stateless).
    실패 시 LLMTimeoutError / LLMCallError를 던지므로,
    호출부(main.py)에서 잡아서 fallback_result로 대체해야 합니다.
    """
    system_prompt = get_system_prompt(package_name, app_name)
    user_content = (
        f"appName: {app_name}\n"
        f"packageName: {package_name}\n"
        f"title: {title or '(없음)'}\n"
        f"content: {content or '(없음)'}\n"
        f"receivedAt: {received_at}"
    )

    try:
        response = await _client.chat.completions.create(
            model=settings.LLM_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_content},
            ],
            response_format={"type": "json_schema", "json_schema": STRUCTURE_SCHEMA},
            timeout=settings.LLM_TIMEOUT_SEC,
        )
    except openai.APITimeoutError as e:
        raise LLMTimeoutError("LLM 응답 시간 초과") from e
    except openai.APIError as e:
        raise LLMCallError(str(e)) from e

    raw = response.choices[0].message.content
    try:
        return json.loads(raw)
    except (TypeError, json.JSONDecodeError) as e:
        raise LLMCallError(f"LLM 응답 JSON 파싱 실패: {e}") from e
