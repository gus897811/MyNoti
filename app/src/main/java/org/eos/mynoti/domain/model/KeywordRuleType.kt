package org.eos.mynoti.domain.model

/**
 * 사용자 키워드 규칙 종류.
 *
 * 기존 DB Spec의 `mapped_type`은 LLM NotificationType을 대체하는 값이 아니다.
 * MUTE는 알림 유형이 아니라 필터 규칙이므로 NotificationType과 분리한다.
 */
enum class KeywordRuleType {
    IMPORTANT,
    MUTE
}
