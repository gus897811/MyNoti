# MyNoti 백엔드 API 명세서

MyNoti 백엔드는 DB를 두지 않는 **Stateless LLM Gateway**입니다. 안드로이드 앱이 알림 원문을
보내면 OpenAI 호출을 대행해 구조화된 분석 결과만 돌려주고, 아무것도 저장하지 않습니다.

- Base URL: `http://localhost:8000` (배포 환경에서는 실제 도메인으로 대체)
- 모든 요청/응답 필드는 **camelCase**입니다.
- 모든 응답의 `type` 값은 안드로이드 Room의 `NotificationType` enum과 동일하게 매핑됩니다.

## 인증

`/api/v1/notifications/*` 하위 모든 엔드포인트는 `X-API-Key` 헤더가 필요합니다.

| Header | 필수 | 설명 |
|---|---|---|
| `X-API-Key` | Y | `.env`의 `API_KEY`와 동일한 값. 없거나 틀리면 `401 UNAUTHORIZED` |

`GET /api/v1/health`는 인증이 필요 없습니다.

## 공통 에러 응답

인증 실패, 잘못된 요청 등은 아래 형식으로 반환됩니다.

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "API 키가 없거나 올바르지 않습니다."
  }
}
```

| status | code | 발생 조건 |
|---|---|---|
| 401 | `UNAUTHORIZED` | `X-API-Key` 헤더 누락 또는 값 불일치 |
| 413 | `PAYLOAD_TOO_LARGE` | `/analyze/batch` 요청에 알림이 20건 초과 |
| 422 | - | FastAPI 기본 유효성 검증 실패 (필수 필드 누락, 타입 불일치 등) |

## 분석 파이프라인 개요

모든 알림은 응답을 반환하기 전 2단계를 거칩니다.

1. **1차 필터링** — 저비용 모델(`LLM_FILTER_MODEL`)이 알림이 사용자에게 보여줄 가치가 있는지
   (`isRelevant`) boolean 하나만 판단합니다. 잡담·안부 인사, 광고/프로모션, 시스템 알림처럼
   실질적 정보가 없다고 판단되면 여기서 걸러지고, **본 분석 모델은 호출되지 않습니다.**
   판단이 애매하거나 필터 호출 자체가 실패/타임아웃되면 항상 통과시킵니다(fail-open) —
   중요한 알림을 실수로 숨기는 것을 방지하기 위함입니다.
2. **본 분석** — 필터를 통과한 알림만 `LLM_MODEL`로 보내 구조화된 분석(JSON) 결과를 생성합니다.
   앱 종류(LearningX/헤이영/카카오톡/Instagram DM/금융 앱 등)에 따라 프롬프트가 분기됩니다.

1차 필터링에서 걸러진 알림은 **분석 정보를 전혀 제공하지 않습니다.** 안드로이드는 아래
각 엔드포인트의 "필터링된 경우" 응답 형식을 보고 해당 알림을 저장/표시하지 않고 버려야 합니다.

---

## POST /api/v1/notifications/analyze

알림 1건을 분석합니다. 서버에는 아무것도 저장되지 않습니다.

### Request

```json
{
  "appName": "LearningX Student",
  "packageName": "com.instructure.candroid.xinics2.production",
  "title": "운영체제 과제 제출 안내",
  "content": "운영체제 과제 2를 8월 14일 23:59까지 제출하세요.",
  "receivedAt": "2026-08-13T10:30:00+09:00"
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `appName` | string | Y | 알림을 보낸 앱의 표시 이름 |
| `packageName` | string | Y | 알림을 보낸 앱의 패키지명. 앱별 프롬프트 분기 기준 |
| `title` | string \| null | N | 알림 원문 제목 |
| `content` | string \| null | N | 알림 원문 본문 |
| `receivedAt` | datetime (ISO 8601) | Y | 알림 수신 시각 |

### Response `200 OK`

정상 분석된 경우:

```json
{
  "summary": "운영체제 과제 2를 8월 14일 23:59까지 제출해야 합니다.",
  "title": "운영체제 과제 2 제출",
  "isImportant": true,
  "type": "ASSIGNMENT",
  "actionRequired": true,
  "deadline": "2026-08-14T23:59:00+09:00",
  "actions": ["운영체제 과제 2 제출"],
  "isFallback": false,
  "isFiltered": false
}
```

1차 필터링에서 걸러진 경우 (**분석 정보 없음**):

```json
{
  "summary": "",
  "title": "",
  "isImportant": false,
  "type": "ETC",
  "actionRequired": false,
  "deadline": null,
  "actions": [],
  "isFallback": false,
  "isFiltered": true
}
```

> ⚠️ `isFiltered: true`인 경우 `summary`/`title`/`type` 등 나머지 필드는 실제 분석 결과가
> 아닌 빈 값(placeholder)입니다. **안드로이드는 `isFiltered`를 가장 먼저 확인해서 true면
> 나머지 필드를 사용하지 말고 해당 알림을 그대로 버려야 합니다** (저장/표시 금지).

| 필드 | 타입 | 설명 |
|---|---|---|
| `summary` | string | 알림 요약 (1~2문장, 60자 내외). 필터링 시 `""` |
| `title` | string | 재작성된 제목 (20자 내외). 필터링 시 `""` |
| `isImportant` | boolean | LLM의 1차 중요도 판단. 최종 중요도는 안드로이드에서 사용자 키워드 규칙과 함께 결정 |
| `type` | enum | `CLASS` \| `ASSIGNMENT` \| `COMMUNICATION` \| `FINANCIAL` \| `ETC` |
| `actionRequired` | boolean | 사용자가 취해야 할 행동이 있는지 여부 |
| `deadline` | datetime \| null | ISO 8601 (KST, `+09:00`). 명확한 기한이 없으면 `null` |
| `actions` | string[] | 필요한 행동 목록. 없으면 빈 배열 |
| `isFallback` | boolean | LLM 호출 실패/타임아웃으로 규칙 기반 폴백 결과가 반환된 경우 true |
| `isFiltered` | boolean | 1차 필터링에서 잡담/불필요 알림으로 판정되어 본 분석을 건너뛴 경우 true |

---

## POST /api/v1/notifications/analyze/batch

알림 여러 건을 한 번에 분석합니다. `NotificationListenerService`가 직접 호출하지 않고,
안드로이드 WorkManager가 쌓인 알림을 모아 주기적으로 호출하는 것을 전제로 합니다.
**최대 20건**까지 요청 가능합니다.

### Request

```json
{
  "notifications": [
    {
      "localId": 101,
      "appName": "LearningX Student",
      "packageName": "com.instructure.candroid.xinics2.production",
      "title": "과제 공지",
      "content": "운영체제 과제 2가 등록되었습니다.",
      "receivedAt": "2026-08-13T10:30:00+09:00"
    },
    {
      "localId": 102,
      "appName": "KakaoTalk",
      "packageName": "com.kakao.talk",
      "title": "수아",
      "content": "ㅋㅋㅋㅋㅋ",
      "receivedAt": "2026-08-13T10:31:00+09:00"
    }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `notifications` | array (최대 20) | Y | 아래 항목의 배열 |
| `notifications[].localId` | int | Y | 안드로이드 Room의 로컬 ID. 응답에서 결과 매칭용으로 그대로 반환됨 |
| `notifications[].appName` | string | Y | 위와 동일 |
| `notifications[].packageName` | string | Y | 위와 동일 |
| `notifications[].title` | string \| null | N | 위와 동일 |
| `notifications[].content` | string \| null | N | 위와 동일 |
| `notifications[].receivedAt` | datetime | Y | 위와 동일 |

### Response `200 OK`

```json
{
  "results": [
    {
      "localId": 101,
      "summary": "운영체제 과제 2가 새로 등록되었습니다.",
      "title": "운영체제 과제 2 등록",
      "isImportant": false,
      "type": "ASSIGNMENT",
      "actionRequired": true,
      "deadline": null,
      "actions": ["운영체제 과제 2 확인"],
      "isFallback": false
    }
  ],
  "failed": [
    { "localId": 103, "reason": "LLM_TIMEOUT" }
  ],
  "filtered": [
    { "localId": 102 }
  ]
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `results` | array | 분석에 성공한 알림 목록. `localId` + `/analyze` 응답 필드(단, `isFiltered` 없음) |
| `failed` | array | 본 분석 LLM 호출이 실패/타임아웃한 알림. `reason`: `LLM_TIMEOUT` \| `LLM_CALL_ERROR` |
| `filtered` | array | 1차 필터링에서 잡담/불필요 알림으로 판정되어 `results`에서 아예 제외된 알림. `localId` 외 정보 없음 |

> ⚠️ 필터링된 알림은 `results`에 포함되지 않고 `filtered` 리스트에 `localId`만 담겨 반환됩니다.
> 안드로이드는 `filtered`에 포함된 `localId`를 저장/표시하지 말고 그대로 버려야 합니다.

각 알림은 `localId` 기준으로 `results` / `failed` / `filtered` 중 정확히 한 곳에만 존재합니다.

---

## GET /api/v1/health

헬스 체크. 인증 불필요.

### Response `200 OK`

```json
{
  "status": "ok",
  "timestamp": "2026-08-15T12:00:00Z"
}
```

---

## 참고

- 요청/응답 필드는 전부 camelCase라 Kotlin data class에 별도 매핑 없이 Retrofit +
  kotlinx.serialization(또는 Gson)으로 바로 역직렬화할 수 있습니다.
- `isImportant`는 LLM의 1차 판단이며, 최종 중요도는 안드로이드에서 사용자의
  Highlight/Mute Keyword 규칙과 함께 고려해서 결정해야 합니다 (백엔드는 사용자 키워드 규칙을 모릅니다).
- 서버는 알림 원문/분석 결과를 저장하지 않습니다. Room이 사용자 데이터의 원본(Source of Truth)입니다.
