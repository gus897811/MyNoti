package org.eos.mynoti.ui.calendar

import org.eos.mynoti.domain.model.CalendarEvent
import org.eos.mynoti.domain.model.NotificationType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class CalendarDayMarker(
    val type: NotificationType,
    val isImportant: Boolean
)

fun dayMarkers(events: List<CalendarEvent>, limit: Int = 3): List<CalendarDayMarker> {
    return events
        .sortedWith(
            compareByDescending<CalendarEvent> { it.isImportant }
                .thenBy { it.eventAt }
        )
        .take(limit)
        .map { CalendarDayMarker(type = it.type, isImportant = it.isImportant) }
}

fun YearMonth.firstDayOffset(firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): Int {
    val first = atDay(1).dayOfWeek
    return (first.value % 7 - firstDayOfWeek.value % 7 + 7) % 7
}

fun YearMonth.daysInMonth(): Int = lengthOfMonth()

fun YearMonth.calendarDays(firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): List<LocalDate?> {
    val offset = firstDayOffset(firstDayOfWeek)
    val days = (1..lengthOfMonth()).map { atDay(it) }
    return List(offset) { null } + days
}

fun YearMonth.shiftedSelection(current: LocalDate): LocalDate {
    return if (current.dayOfMonth > lengthOfMonth()) {
        atDay(1)
    } else {
        atDay(current.dayOfMonth)
    }
}
