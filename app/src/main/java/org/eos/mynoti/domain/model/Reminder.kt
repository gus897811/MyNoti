package org.eos.mynoti.domain.model

import java.time.LocalDateTime

data class Reminder(
    val id: Long,
    val notificationId: Long,
    val remindAt: LocalDateTime,
    val isFired: Boolean,
    val createdAt: LocalDateTime
)

data class ReminderItem(
    val id: Long,
    val notificationId: Long,
    val title: String,
    val appName: String,
    val remindAt: LocalDateTime,
    val isFired: Boolean
)

data class ReminderTimeGroup(
    val remindAt: LocalDateTime,
    val items: List<ReminderItem>
)

object ReminderGroupFactory {
    fun group(items: List<ReminderItem>): List<ReminderTimeGroup> {
        return items
            .sortedWith(compareBy<ReminderItem> { it.remindAt }.thenBy { it.id })
            .groupBy { it.remindAt.withSecond(0).withNano(0) }
            .map { (time, grouped) ->
                ReminderTimeGroup(
                    remindAt = time,
                    items = grouped.sortedBy { it.id }
                )
            }
            .sortedBy { it.remindAt }
    }
}
