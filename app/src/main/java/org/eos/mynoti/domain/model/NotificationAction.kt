package org.eos.mynoti.domain.model

data class NotificationAction(
    val id: Long,
    val title: String,
    val isCompleted: Boolean = false
)
