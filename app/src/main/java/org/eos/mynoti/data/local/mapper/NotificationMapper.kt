package org.eos.mynoti.data.local.mapper

import org.eos.mynoti.data.local.entity.NotificationEntity
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationAction

fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = notificationId,
        appName = appName,
        appPackageName = appPackageName,
        title = title.orEmpty(),
        content = content.orEmpty(),
        summary = summary,
        receivedAt = receivedAt,
        isImportant = isImportant,
        type = type,
        remindAt = remindAt,
        isReminded = isReminded,
        actions = actions.mapIndexed { index, title ->
            NotificationAction(id = index.toLong(), title = title)
        },
        actionRequired = actionRequired,
        analysisStatus = analysisStatus
    )
}

fun Notification.toEntity(createdAt: java.time.LocalDateTime = receivedAt): NotificationEntity {
    return NotificationEntity(
        notificationId = id,
        appName = appName,
        appPackageName = appPackageName,
        title = title,
        content = content,
        receivedAt = receivedAt,
        isImportant = isImportant,
        type = type,
        createdAt = createdAt,
        remindAt = remindAt,
        isReminded = isReminded,
        summary = summary,
        actionRequired = actionRequired,
        analysisStatus = analysisStatus,
        actions = actions.map { it.title }
    )
}
