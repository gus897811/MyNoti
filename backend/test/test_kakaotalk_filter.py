"""카카오톡 잡담 알림이 1차 필터에서 isFiltered로 걸러지는지 확인합니다."""

from fastapi.testclient import TestClient


def test_kakaotalk_dot_message_is_filtered(
    client: TestClient, api_headers: dict[str, str]
):
    response = client.post(
        "/api/v1/notifications/analyze",
        headers=api_headers,
        json={
            "appName": "KakaoTalk",
            "packageName": "com.kakao.talk",
            "title": "친구",
            "content": ".",
            "receivedAt": "2026-08-15T10:30:00+09:00",
        },
    )

    assert response.status_code == 200, response.text
    body = response.json()
    assert body["isFiltered"] is True, f"필터링되지 않음: {body}"
    assert body["summary"] == ""
    assert body["title"] == ""
    assert body["isFallback"] is False
