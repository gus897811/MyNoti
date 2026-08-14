package org.eos.mynoti.ui.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

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
