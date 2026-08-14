package org.eos.mynoti.data.local.mapper

import org.eos.mynoti.data.local.entity.ManualCalendarEventEntity
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.CalendarEvent

fun ManualCalendarEventEntity.toCalendarEvent(): CalendarEvent {
    return CalendarEvent(
        manualEventId = eventId,
        title = title,
        location = location?.takeIf { it.isNotBlank() },
        eventAt = eventAt,
        receivedAt = null,
        appName = "직접 추가",
        appPackageName = AppPackages.MANUAL,
        type = type,
        isImportant = isImportant
    )
}
