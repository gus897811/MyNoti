from datetime import datetime, timezone

from fastapi import Depends, FastAPI, Request
from fastapi.responses import JSONResponse

from app.auth import verify_api_key
from app.errors import APIError
from app.llm_service import (
    LLMCallError,
    LLMTimeoutError,
    analyze_notification,
    fallback_result,
    is_notification_relevant,
)
from app.models import (
    AnalyzeRequest,
    AnalyzeResponse,
    BatchAnalyzeRequest,
    BatchAnalyzeResponse,
    BatchFailedItem,
    BatchFilteredItem,
    BatchResultItem,
    HealthResponse,
    NotificationType,
)

app = FastAPI(title="MyNoti 백엔드 (LLM Gateway)", version="0.2.0")


@app.exception_handler(APIError)
async def api_error_handler(request: Request, exc: APIError):
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": {"code": exc.code, "message": exc.message}},
    )


# ---- POST /api/v1/notifications/analyze ----
# 알림 1건을 분석만 하고 서버에는 아무것도 저장하지 않습니다 (Stateless).

@app.post(
    "/api/v1/notifications/analyze",
    response_model=AnalyzeResponse,
    dependencies=[Depends(verify_api_key)],
)
async def analyze(req: AnalyzeRequest):
    is_relevant = await is_notification_relevant(
        req.appName, req.packageName, req.title, req.content
    )
    if not is_relevant:
        return AnalyzeResponse(
            summary="",
            title="",
            isImportant=False,
            type=NotificationType.ETC,
            actionRequired=False,
            deadline=None,
            actions=[],
            isFallback=False,
            isFiltered=True,
        )

    try:
        result = await analyze_notification(
            req.appName,
            req.packageName,
            req.title,
            req.content,
            req.receivedAt.isoformat(),
        )
        is_fallback = False
    except (LLMTimeoutError, LLMCallError):
        result = fallback_result(req.title, req.content)
        is_fallback = True

    return AnalyzeResponse(**result, isFallback=is_fallback, isFiltered=False)


# ---- POST /api/v1/notifications/analyze/batch ----
# NotificationListenerService가 직접 호출하지 않고, 안드로이드의 WorkManager가
# 쌓인 알림을 모아 주기적으로 호출하는 것을 전제로 합니다.

@app.post(
    "/api/v1/notifications/analyze/batch",
    response_model=BatchAnalyzeResponse,
    dependencies=[Depends(verify_api_key)],
)
async def analyze_batch(req: BatchAnalyzeRequest):
    if len(req.notifications) > 20:
        raise APIError(413, "PAYLOAD_TOO_LARGE", "batch 요청은 최대 20건까지 가능합니다.")

    results: list[BatchResultItem] = []
    failed: list[BatchFailedItem] = []
    filtered: list[BatchFilteredItem] = []

    for item in req.notifications:
        is_relevant = await is_notification_relevant(
            item.appName, item.packageName, item.title, item.content
        )
        if not is_relevant:
            filtered.append(BatchFilteredItem(localId=item.localId))
            continue

        try:
            result = await analyze_notification(
                item.appName,
                item.packageName,
                item.title,
                item.content,
                item.receivedAt.isoformat(),
            )
            results.append(BatchResultItem(localId=item.localId, **result, isFallback=False))
        except LLMTimeoutError:
            failed.append(BatchFailedItem(localId=item.localId, reason="LLM_TIMEOUT"))
        except LLMCallError:
            failed.append(BatchFailedItem(localId=item.localId, reason="LLM_CALL_ERROR"))

    return BatchAnalyzeResponse(results=results, failed=failed, filtered=filtered)


# ---- GET /api/v1/health ----

@app.get("/api/v1/health", response_model=HealthResponse)
async def health():
    return HealthResponse(status="ok", timestamp=datetime.now(timezone.utc))
