package org.eos.mynoti.domain.model

import java.time.LocalDateTime

data class Notification(
    val id: Long,
    val appName: String,
    val appPackageName: String,
    val title: String,
    val content: String,
    val summary: String?,
    val receivedAt: LocalDateTime,
    val isImportant: Boolean,
    val type: NotificationType,
    val remindAt: LocalDateTime?,
    val isReminded: Boolean,
    val actions: List<NotificationAction> = emptyList()
) {
    fun displaySummary(): String = summary?.takeIf { it.isNotBlank() } ?: content
}
