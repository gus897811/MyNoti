package org.eos.mynoti.data.repository

import org.eos.mynoti.domain.model.Notification

interface NotificationRepository {
    suspend fun getNotifications(): List<Notification>
    suspend fun getNotification(id: Long): Notification?
    suspend fun getImportantNotifications(): List<Notification>
}
