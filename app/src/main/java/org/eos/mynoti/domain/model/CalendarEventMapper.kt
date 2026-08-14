package org.eos.mynoti.domain.model

fun Notification.toCalendarEvent(
    highlightKeywords: List<String> = emptyList()
): CalendarEvent? {
    val deadline = deadline ?: return null
    return CalendarEvent(
        notificationId = id,
        title = title,
        location = null,
        eventAt = deadline,
        receivedAt = receivedAt,
        appName = appName,
        appPackageName = appPackageName,
        type = type,
        isImportant = isEffectivelyImportant(highlightKeywords)
    )
}

fun List<Notification>.toCalendarEvents(
    highlightKeywords: List<String> = emptyList()
): List<CalendarEvent> {
    return mapNotNull { it.toCalendarEvent(highlightKeywords) }
        .sortedBy { it.eventAt }
}
