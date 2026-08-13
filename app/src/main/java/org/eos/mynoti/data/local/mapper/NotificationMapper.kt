package org.eos.mynoti.data.local.mapper

import org.eos.mynoti.data.local.entity.NotificationEntity
import org.eos.mynoti.domain.model.Notification

fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = notificationId,
        appName = appName,
        appPackageName = appPackageName,
        title = title.orEmpty(),
        content = content.orEmpty(),
        // summary/actions는 LLM 분석 컬럼이 확정되면 migration으로 추가한다.
        summary = null,
        receivedAt = receivedAt,
        isImportant = isImportant,
        type = type,
        remindAt = remindAt,
        isReminded = isReminded,
        actions = emptyList()
    )
}

fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        notificationId = id,
        appName = appName,
        appPackageName = appPackageName,
        title = title,
        content = content,
        receivedAt = receivedAt,
        isImportant = isImportant,
        type = type,
        createdAt = receivedAt,
        remindAt = remindAt,
        isReminded = isReminded
    )
}
