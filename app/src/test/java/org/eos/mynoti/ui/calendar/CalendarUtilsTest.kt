package org.eos.mynoti.ui.calendar

import org.eos.mynoti.domain.model.CalendarEvent
import org.eos.mynoti.domain.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class CalendarUtilsTest {

    @Test
    fun august2026StartsOnSaturday() {
        val month = YearMonth.of(2026, 8)
        assertEquals(6, month.firstDayOffset())
        assertEquals(31, month.daysInMonth())
        val days = month.calendarDays()
        assertEquals(6, days.takeWhile { it == null }.size)
        assertEquals(LocalDate.of(2026, 8, 1), days[6])
        assertEquals(LocalDate.of(2026, 8, 31), days.last())
    }

    @Test
    fun leapYearFebruaryHas29Days() {
        val month = YearMonth.of(2024, 2)
        assertEquals(29, month.daysInMonth())
        assertTrue(month.calendarDays().contains(LocalDate.of(2024, 2, 29)))
    }

    @Test
    fun nonLeapFebruaryHas28Days() {
        val month = YearMonth.of(2026, 2)
        assertEquals(28, month.daysInMonth())
    }

    @Test
    fun shiftingFromAugust31SelectsSeptember1() {
        val selected = YearMonth.of(2026, 9).shiftedSelection(LocalDate.of(2026, 8, 31))
        assertEquals(LocalDate.of(2026, 9, 1), selected)
    }

    @Test
    fun shiftingKeepsSameDayWhenItExists() {
        val selected = YearMonth.of(2026, 9).shiftedSelection(LocalDate.of(2026, 8, 10))
        assertEquals(LocalDate.of(2026, 9, 10), selected)
    }

    @Test
    fun dayMarkersEmptyListIsEmpty() {
        assertTrue(dayMarkers(emptyList()).isEmpty())
    }

    @Test
    fun dayMarkersKeepsImportantAndNotImportant() {
        val events = listOf(
            event(hour = 9, type = NotificationType.CLASS, important = false),
            event(hour = 15, type = NotificationType.ASSIGNMENT, important = true)
        )
        val markers = dayMarkers(events)
        assertEquals(2, markers.size)
        assertEquals(CalendarDayMarker(NotificationType.ASSIGNMENT, true), markers[0])
        assertEquals(CalendarDayMarker(NotificationType.CLASS, false), markers[1])
    }

    @Test
    fun dayMarkersLimitsToThreeWithImportantFirst() {
        val events = listOf(
            event(hour = 9, type = NotificationType.CLASS, important = false),
            event(hour = 10, type = NotificationType.ETC, important = false),
            event(hour = 11, type = NotificationType.FINANCIAL, important = false),
            event(hour = 16, type = NotificationType.ASSIGNMENT, important = true)
        )
        val markers = dayMarkers(events)
        assertEquals(3, markers.size)
        assertEquals(CalendarDayMarker(NotificationType.ASSIGNMENT, true), markers[0])
        assertEquals(CalendarDayMarker(NotificationType.CLASS, false), markers[1])
        assertEquals(CalendarDayMarker(NotificationType.ETC, false), markers[2])
    }

    @Test
    fun dayMarkersDoesNotMergeSameTypeWithDifferentImportance() {
        val events = listOf(
            event(hour = 12, type = NotificationType.COMMUNICATION, important = false),
            event(hour = 15, type = NotificationType.COMMUNICATION, important = true)
        )
        val markers = dayMarkers(events)
        assertEquals(2, markers.size)
        assertEquals(CalendarDayMarker(NotificationType.COMMUNICATION, true), markers[0])
        assertEquals(CalendarDayMarker(NotificationType.COMMUNICATION, false), markers[1])
    }

    private fun event(
        hour: Int,
        type: NotificationType,
        important: Boolean
    ): CalendarEvent {
        return CalendarEvent(
            title = "event",
            eventAt = LocalDateTime.of(2026, 8, 10, hour, 0),
            appName = "app",
            appPackageName = "pkg",
            type = type,
            isImportant = important
        )
    }
}
