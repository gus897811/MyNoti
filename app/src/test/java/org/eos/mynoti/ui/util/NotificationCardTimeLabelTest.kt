package org.eos.mynoti.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class NotificationCardTimeLabelTest {

    private val today = LocalDate.of(2026, 8, 15)

    @Test
    fun todayAfternoonShowsTimeOnly() {
        val receivedAt = LocalDateTime.of(2026, 8, 15, 15, 0)
        assertEquals("오후 3:00", receivedAt.toNotificationCardTimeLabel(today))
        assertEquals(receivedAt.toReceivedTimeLabel(), receivedAt.toNotificationCardTimeLabel(today))
    }

    @Test
    fun todayMidnightShowsTimeOnly() {
        val receivedAt = LocalDateTime.of(2026, 8, 15, 0, 0)
        val label = receivedAt.toNotificationCardTimeLabel(today)
        assertEquals(receivedAt.toReceivedTimeLabel(), label)
        assertEquals("오전 12:00", label)
    }

    @Test
    fun todayMorningShowsTimeOnly() {
        val receivedAt = LocalDateTime.of(2026, 8, 15, 9, 5)
        assertEquals("오전 9:05", receivedAt.toNotificationCardTimeLabel(today))
        assertEquals(receivedAt.toReceivedTimeLabel(), receivedAt.toNotificationCardTimeLabel(today))
    }

    @Test
    fun yesterdaySameYearIncludesDate() {
        val receivedAt = LocalDateTime.of(2026, 8, 14, 15, 0)
        assertEquals("8월 14일 오후 3:00", receivedAt.toNotificationCardTimeLabel(today))
    }

    @Test
    fun sameYearJanuaryIncludesDateWithoutPadding() {
        val receivedAt = LocalDateTime.of(2026, 1, 1, 9, 5)
        assertEquals("1월 1일 오전 9:05", receivedAt.toNotificationCardTimeLabel(today))
    }

    @Test
    fun previousYearIncludesYear() {
        val receivedAt = LocalDateTime.of(2025, 8, 15, 15, 0)
        assertEquals("2025년 8월 15일 오후 3:00", receivedAt.toNotificationCardTimeLabel(today))
    }

    @Test
    fun nextYearIncludesYear() {
        val receivedAt = LocalDateTime.of(2027, 12, 31, 23, 59)
        assertEquals("2027년 12월 31일 오후 11:59", receivedAt.toNotificationCardTimeLabel(today))
    }

    @Test
    fun receivedTimeLabelIsUnchanged() {
        val receivedAt = LocalDateTime.of(2026, 8, 14, 15, 0)
        assertEquals("오후 3:00", receivedAt.toReceivedTimeLabel())
    }
}
