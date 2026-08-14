package org.eos.mynoti.ui.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
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
}
