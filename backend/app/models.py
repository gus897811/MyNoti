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
    # 1차 필터링에서 잡담/불필요 알림으로 판정된 경우 true.
    # true일 때 summary/title 등 나머지 필드는 실제 분석 결과가 아닌 빈 값이므로
    # 안드로이드는 isFiltered부터 확인해서 true면 나머지 필드를 쓰지 말고 그대로 버려야 합니다.
    isFiltered: bool = False


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


class BatchFilteredItem(BaseModel):
    # 1차 필터링에서 잡담/불필요 알림으로 판정되어 본 분석을 건너뛴 항목.
    # 분석 정보를 전혀 제공하지 않으므로 localId만 담습니다.
    localId: int


class BatchAnalyzeResponse(BaseModel):
    results: List[BatchResultItem]
    failed: List[BatchFailedItem]
    filtered: List[BatchFilteredItem] = Field(default_factory=list)


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
