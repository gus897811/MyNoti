package org.eos.mynoti.ui.util

import org.eos.mynoti.domain.model.Notification
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class NotificationDateGroup(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    EARLIER("Earlier")
}

fun Notification.previewText(maxLength: Int = 90): String {
    val singleLine = displaySummary().replace('\n', ' ').trim()
    return if (singleLine.length <= maxLength) {
        singleLine
    } else {
        singleLine.take(maxLength).trimEnd() + "…"
    }
}

fun LocalDateTime.toReceivedTimeLabel(): String {
    val formatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
    return format(formatter)
}

fun LocalDateTime.toReceivedTimestamp(): String {
    return format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}

fun LocalDateTime.toReminderGroupLabel(today: LocalDate = LocalDate.now()): String {
    val time = format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN))
    val dateLabel = when (toLocalDate()) {
        today -> "오늘"
        today.plusDays(1) -> "내일"
        else -> format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN))
    }
    return "$dateLabel $time"
}

fun LocalDate.toDateGroup(today: LocalDate = LocalDate.now()): NotificationDateGroup {
    return when (this) {
        today -> NotificationDateGroup.TODAY
        today.minusDays(1) -> NotificationDateGroup.YESTERDAY
        else -> NotificationDateGroup.EARLIER
    }
}

fun List<Notification>.groupedByDate(
    today: LocalDate = LocalDate.now()
): List<Pair<NotificationDateGroup, List<Notification>>> {
    return NotificationDateGroup.entries.mapNotNull { group ->
        val items = filter { it.receivedAt.toLocalDate().toDateGroup(today) == group }
            .sortedByDescending { it.receivedAt }
        if (items.isEmpty()) null else group to items
    }
}
