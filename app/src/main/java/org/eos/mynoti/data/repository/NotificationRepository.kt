package org.eos.mynoti.data.repository

import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.domain.model.Notification
import java.time.LocalDateTime

interface NotificationRepository {

    fun observeNotifications(): Flow<List<Notification>>

    fun observeImportantNotifications(): Flow<List<Notification>>

    fun observeNotificationsByApp(packageName: String): Flow<List<Notification>>

    fun observeNotification(id: Long): Flow<Notification?>

    suspend fun getNotifications(): List<Notification>

    suspend fun getNotification(id: Long): Notification?

    suspend fun getImportantNotifications(): List<Notification>

    suspend fun insertNotification(notification: Notification): Long

    suspend fun updateNotification(notification: Notification)

    suspend fun deleteNotification(id: Long)

    suspend fun setImportant(id: Long, isImportant: Boolean)

    suspend fun setReminder(id: Long, remindAt: LocalDateTime)
}
