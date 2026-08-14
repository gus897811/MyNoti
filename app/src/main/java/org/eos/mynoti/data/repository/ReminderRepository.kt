package org.eos.mynoti.data.repository

import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.domain.model.Reminder
import org.eos.mynoti.domain.model.ReminderItem
import java.time.LocalDateTime

interface ReminderRepository {

    fun observeByNotificationId(notificationId: Long): Flow<List<Reminder>>

    fun observeVisibleItems(): Flow<List<ReminderItem>>

    suspend fun schedule(notificationId: Long, remindAt: LocalDateTime): Long

    suspend fun cancel(reminderId: Long)

    suspend fun fire(reminderId: Long)

    suspend fun reschedulePending()
}
