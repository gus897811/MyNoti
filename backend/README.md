# MyNoti 백엔드 (Python + FastAPI, Stateless LLM Gateway)

ChatGPT와 나눈 설계 대화를 기준으로 만든 백엔드입니다. **DB를 두지 않고 OpenAI 호출만 대행**합니다.
알림 원문/분석 결과는 전부 안드로이드 Room에 저장되며, 서버는 요청받은 알림 1건(또는 여러 건)을
분석해서 결과만 돌려주고 아무것도 저장하지 않습니다.

## 실행 방법

```bash
python -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate

pip install -r requirements.txt

cp .env.example .env
# .env 파일을 열어 OPENAI_API_KEY, API_KEY 값을 채워넣으세요
# LLM_MODEL은 https://platform.openai.com/docs/models 에서 최신 모델 ID로 확인 후 설정하세요

uvicorn app.main:app --reload --port 8000
```

`http://localhost:8000/docs`에서 Swagger UI로 바로 테스트할 수 있습니다.

## 테스트 (curl)

### 알림 1건 분석

```bash
curl -X POST http://localhost:8000/api/v1/notifications/analyze \
  -H "X-API-Key: dev-api-key-change-me" \
  -H "Content-Type: application/json" \
  -d '{
    "appName": "LearningX Student",
    "packageName": "com.learningx.student",
    "title": "운영체제 과제 제출 안내",
    "content": "운영체제 과제 2를 8월 14일 23:59까지 제출하세요.",
    "receivedAt": "2026-08-13T10:30:00+09:00"
  }'
```

응답 예시:

```json
{
  "summary": "운영체제 과제 2를 8월 14일 23:59까지 제출해야 합니다.",
  "isImportant": true,
  "type": "ASSIGNMENT",
  "actionRequired": true,
  "deadline": "2026-08-14T23:59:00+09:00",
  "actions": ["운영체제 과제 2 제출"],
  "isFallback": false
}
```

### 알림 여러 건 일괄 분석 (WorkManager용)

```bash
curl -X POST http://localhost:8000/api/v1/notifications/analyze/batch \
  -H "X-API-Key: dev-api-key-change-me" \
  -H "Content-Type: application/json" \
  -d '{
    "notifications": [
      {
        "localId": 101,
        "appName": "LearningX Student",
        "packageName": "com.learningx.student",
        "title": "과제 공지",
        "content": "운영체제 과제 2가 등록되었습니다.",
        "receivedAt": "2026-08-13T10:30:00+09:00"
      }
    ]
  }'
```

### 헬스 체크

```bash
curl http://localhost:8000/api/v1/health
```

## 안드로이드 연동 시 참고

- 요청/응답 필드가 전부 **camelCase**라서 Kotlin data class에 별도 매핑 없이 Retrofit + kotlinx.serialization(또는 Gson)으로 바로 역직렬화할 수 있습니다.
- `NotificationListenerService`에서 이 API를 직접 호출하지 마세요. Room에 원문을 먼저 저장한 뒤, **WorkManager**가 분석 대기 상태인 알림을 모아 `/analyze` 또는 `/analyze/batch`를 호출하고, 응답을 받으면 Room을 UPDATE하는 흐름을 권장합니다.
- 응답의 `type` 값(`CLASS`/`ASSIGNMENT`/`COMMUNICATION`/`FINANCIAL`/`ETC`)은 Room의 `NotificationType` enum과 동일한 값이라 그대로 저장하면 됩니다.
- `isImportant`는 LLM의 1차 판단입니다. 최종 중요도는 안드로이드에서 사용자의 Highlight/Mute Keyword 규칙과 함께 고려해서 결정하세요 (백엔드는 사용자 키워드 규칙을 모릅니다).

## 파일 구조

```
mynoti-backend/
├── app/
│   ├── main.py          # 라우트 정의 (analyze / analyze-batch / health)
│   ├── models.py        # 요청/응답 Pydantic 스키마 (camelCase)
│   ├── llm_service.py   # OpenAI 연동, Structured Outputs로 JSON 강제, 폴백 처리
│   ├── prompts.py       # 공통 분류 기준 + 앱별(LearningX/헤이영/카톡/금융) 상세 프롬프트
│   ├── auth.py          # X-API-Key 검증
│   ├── config.py        # 환경변수
│   └── errors.py        # 공통 에러 클래스
├── requirements.txt
├── .env.example
└── README.md
```

## 대화에서 확정된 설계 원칙 (지켜야 할 것)

- ❌ 백엔드에 DB를 두지 않는다 (알림 원문/분석 결과 저장 금지)
- ❌ NotificationListenerService에서 직접 API를 호출하지 않는다
- ❌ OpenAI API 키를 안드로이드 앱에 넣지 않는다 (`.env`에만 존재)
- ✅ 사용자 Keyword Rule(Highlight/Mute) 적용은 안드로이드에서 처리한다
- ✅ Room 데이터가 사용자 데이터의 원본(Source of Truth)이다

## Day 1~4 진행 시 체크할 것

- [ ] `app/prompts.py`의 `_PACKAGE_KEYWORD_MAP`에 실제 LearningX/헤이영/금융 앱 패키지명 반영
- [ ] `.env`의 `OPENAI_API_KEY` 발급 및 등록
- [ ] `LLM_MODEL` 값이 현재 사용 가능한 모델 ID인지 확인
- [ ] 배포 시 `API_KEY`를 추측 불가능한 값으로 변경
- [ ] batch 처리 지연이 크면 `LLM_TIMEOUT_SEC` 조정
