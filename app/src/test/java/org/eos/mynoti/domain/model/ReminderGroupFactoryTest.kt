package org.eos.mynoti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ReminderGroupFactoryTest {

    @Test
    fun groupsByMinuteAndKeepsTimeOrder() {
        val noon = LocalDateTime.of(2026, 8, 14, 12, 0, 30)
        val evening = LocalDateTime.of(2026, 8, 14, 20, 0)
        val items = listOf(
            item(id = 2, title = "저녁 알림", remindAt = evening),
            item(id = 1, title = "점심 알림 A", remindAt = noon),
            item(id = 3, title = "점심 알림 B", remindAt = noon.withSecond(10))
        )

        val groups = ReminderGroupFactory.group(items)

        assertEquals(2, groups.size)
        assertEquals(LocalDateTime.of(2026, 8, 14, 12, 0), groups[0].remindAt)
        assertEquals(listOf(1L, 3L), groups[0].items.map { it.id })
        assertEquals(LocalDateTime.of(2026, 8, 14, 20, 0), groups[1].remindAt)
        assertEquals(listOf(2L), groups[1].items.map { it.id })
    }

    private fun item(
        id: Long,
        title: String,
        remindAt: LocalDateTime
    ) = ReminderItem(
        id = id,
        notificationId = id,
        title = title,
        appName = "LearningX",
        remindAt = remindAt,
        isFired = false
    )
}
