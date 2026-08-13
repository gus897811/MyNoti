package org.eos.mynoti.data.mock

import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.domain.model.Notification
import java.time.LocalDateTime

class MockNotificationRepository(
    now: LocalDateTime = LocalDateTime.now()
) : NotificationRepository {

    private val notifications: List<Notification> = MockNotificationData.create(now)

    override suspend fun getNotifications(): List<Notification> {
        return notifications.sortedByDescending { it.receivedAt }
    }

    override suspend fun getNotification(id: Long): Notification? {
        return notifications.find { it.id == id }
    }

    override suspend fun getImportantNotifications(): List<Notification> {
        return notifications
            .filter { it.isImportant }
            .sortedByDescending { it.receivedAt }
    }
}
