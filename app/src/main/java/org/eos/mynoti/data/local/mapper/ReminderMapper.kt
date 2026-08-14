package org.eos.mynoti.data.local.mapper

import org.eos.mynoti.data.local.entity.ReminderEntity
import org.eos.mynoti.data.local.entity.ReminderWithNotificationRow
import org.eos.mynoti.domain.model.Reminder
import org.eos.mynoti.domain.model.ReminderItem

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = reminderId,
        notificationId = notificationId,
        remindAt = remindAt,
        isFired = isFired,
        createdAt = createdAt
    )
}

fun ReminderWithNotificationRow.toItem(): ReminderItem {
    return ReminderItem(
        id = reminder.reminderId,
        notificationId = reminder.notificationId,
        title = title?.takeIf { it.isNotBlank() } ?: appName,
        appName = appName,
        remindAt = reminder.remindAt,
        isFired = reminder.isFired
    )
}
