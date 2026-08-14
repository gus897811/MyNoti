package org.eos.mynoti.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class CalendarEvent(
    val notificationId: Long? = null,
    val manualEventId: Long? = null,
    val title: String,
    val location: String? = null,
    val eventAt: LocalDateTime,
    val receivedAt: LocalDateTime? = null,
    val appName: String,
    val appPackageName: String,
    val type: NotificationType,
    val isImportant: Boolean
) {
    val date: LocalDate get() = eventAt.toLocalDate()

    val listKey: String
        get() = when {
            notificationId != null -> "n-$notificationId"
            else -> "m-${manualEventId ?: 0}"
        }
}
