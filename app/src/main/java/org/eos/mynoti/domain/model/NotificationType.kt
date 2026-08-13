package org.eos.mynoti.domain.model

enum class NotificationType(val label: String) {
    CLASS("수업"),
    ASSIGNMENT("과제"),
    COMMUNICATION("커뮤니케이션"),
    FINANCIAL("금융"),
    ETC("기타")
}
