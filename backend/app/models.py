from datetime import datetime
from enum import Enum
from typing import List, Optional

from pydantic import BaseModel, Field


class NotificationType(str, Enum):
    """DB Spec의 notification.type과 동일한 값. MUTE는 타입이 아니라
    사용자 설정 개념이므로 여기 포함하지 않습니다 (대화에서 확정된 사항)."""

    CLASS = "CLASS"
    ASSIGNMENT = "ASSIGNMENT"
    COMMUNICATION = "COMMUNICATION"
    FINANCIAL = "FINANCIAL"
    ETC = "ETC"


# ---- POST /api/v1/notifications/analyze ----

class AnalyzeRequest(BaseModel):
    appName: str
    packageName: str
    title: Optional[str] = None
    content: Optional[str] = None
    receivedAt: datetime


class AnalyzeResponse(BaseModel):
    summary: str
    title: str
    isImportant: bool
    type: NotificationType
    actionRequired: bool
    deadline: Optional[datetime] = None
    actions: List[str] = Field(default_factory=list)
    # 원래 대화 스펙에는 없던 필드지만, LLM 실패 시 폴백 결과인지 안드로이드가
    # 구분할 수 있도록 추가했습니다. 필요 없으면 지워도 다른 필드에 영향 없습니다.
    isFallback: bool = False


# ---- POST /api/v1/notifications/analyze/batch ----

class BatchNotificationItem(BaseModel):
    localId: int
    appName: str
    packageName: str
    title: Optional[str] = None
    content: Optional[str] = None
    receivedAt: datetime


class BatchAnalyzeRequest(BaseModel):
    notifications: List[BatchNotificationItem] = Field(..., max_length=20)


class BatchResultItem(BaseModel):
    localId: int
    summary: str
    title: str
    isImportant: bool
    type: NotificationType
    actionRequired: bool
    deadline: Optional[datetime] = None
    actions: List[str] = Field(default_factory=list)
    isFallback: bool = False


class BatchFailedItem(BaseModel):
    localId: int
    reason: str


class BatchAnalyzeResponse(BaseModel):
    results: List[BatchResultItem]
    failed: List[BatchFailedItem]


# ---- GET /api/v1/health ----

class HealthResponse(BaseModel):
    status: str
    timestamp: datetime


# ---- 공통 에러 ----

class ErrorDetail(BaseModel):
    code: str
    message: str


class ErrorResponse(BaseModel):
    error: ErrorDetail
