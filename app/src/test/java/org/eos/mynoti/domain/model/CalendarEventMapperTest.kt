package org.eos.mynoti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class CalendarEventMapperTest {

    @Test
    fun mapsDeadlineToEventDateAndKeepsReceivedAt() {
        val received = LocalDateTime.of(2026, 8, 10, 8, 40)
        val deadline = LocalDateTime.of(2026, 8, 10, 15, 0)
        val notification = Notification(
            id = 6,
            appName = "카카오톡",
            appPackageName = AppPackages.KAKAOTALK,
            title = "캡스톤 팀플 회의",
            content = "오늘 오후 3시에 중앙도서관에서 만나요.",
            summary = "오후 3시 캡스톤 팀플",
            receivedAt = received,
            isImportant = true,
            type = NotificationType.COMMUNICATION,
            deadline = deadline
        )

        val event = notification.toCalendarEvent()
        assertEquals(deadline.toLocalDate(), event?.date)
        assertEquals(deadline, event?.eventAt)
        assertEquals(received, event?.receivedAt)
        assertEquals("캡스톤 팀플 회의", event?.title)
        assertEquals(AppPackages.KAKAOTALK, event?.appPackageName)
        assertEquals(6L, event?.notificationId)
        assertNull(event?.location)
    }

    @Test
    fun excludesNotificationsWithoutDeadline() {
        val notification = Notification(
            id = 2,
            appName = "LearningX",
            appPackageName = AppPackages.LEARNING_X,
            title = "자료구조 강의 공지",
            content = "공지",
            summary = null,
            receivedAt = LocalDateTime.of(2026, 8, 10, 10, 0),
            isImportant = false,
            type = NotificationType.CLASS,
            deadline = null
        )
        assertNull(notification.toCalendarEvent())
    }

    @Test
    fun highlightKeywordMarksEventImportant() {
        val notification = Notification(
            id = 3,
            appName = "LearningX",
            appPackageName = AppPackages.LEARNING_X,
            title = "과제 안내",
            content = "제출하세요",
            summary = null,
            receivedAt = LocalDateTime.of(2026, 8, 10, 10, 0),
            isImportant = false,
            type = NotificationType.ASSIGNMENT,
            deadline = LocalDateTime.of(2026, 8, 10, 23, 59)
        )
        val event = notification.toCalendarEvent(highlightKeywords = listOf("과제"))
        assertTrue(event?.isImportant == true)
    }
}
