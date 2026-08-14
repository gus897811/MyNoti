package org.eos.mynoti.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.eos.mynoti.data.local.dao.NotificationDao
import org.eos.mynoti.data.local.dao.ReminderDao
import org.eos.mynoti.data.local.entity.ReminderEntity
import org.eos.mynoti.data.local.mapper.toDomain
import org.eos.mynoti.data.local.mapper.toItem
import org.eos.mynoti.data.reminder.ReminderNotifier
import org.eos.mynoti.data.reminder.ReminderScheduler
import org.eos.mynoti.data.repository.ReminderRepository
import org.eos.mynoti.domain.model.Reminder
import org.eos.mynoti.domain.model.ReminderItem
import java.time.LocalDate
import java.time.LocalDateTime

class RoomReminderRepository(
    private val reminderDao: ReminderDao,
    private val notificationDao: NotificationDao,
    private val scheduler: ReminderScheduler,
    private val notifier: ReminderNotifier
) : ReminderRepository {

    override fun observeByNotificationId(notificationId: Long): Flow<List<Reminder>> {
        return reminderDao.observeByNotificationId(notificationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeVisibleItems(): Flow<List<ReminderItem>> {
        return reminderDao.observeAllWithNotification().map { rows ->
            val startOfToday = LocalDate.now().atStartOfDay()
            rows.map { it.toItem() }
                .filter { item -> !item.isFired || !item.remindAt.isBefore(startOfToday) }
        }
    }

    override suspend fun schedule(notificationId: Long, remindAt: LocalDateTime): Long {
        val id = reminderDao.insert(
            ReminderEntity(
                notificationId = notificationId,
                remindAt = remindAt,
                isFired = false,
                createdAt = LocalDateTime.now()
            )
        )
        scheduler.schedule(id, remindAt)
        return id
    }

    override suspend fun cancel(reminderId: Long) {
        scheduler.cancel(reminderId)
        reminderDao.deleteById(reminderId)
    }

    override suspend fun fire(reminderId: Long) {
        val entity = reminderDao.getById(reminderId) ?: return
        if (entity.isFired) return
        val notification = notificationDao.getById(entity.notificationId)?.let { stored ->
            stored.toDomain()
        } ?: return
        notifier.show(entity.toDomain(), notification)
        reminderDao.markFired(reminderId)
    }

    override suspend fun reschedulePending() {
        reminderDao.getPending().forEach { reminder ->
            scheduler.schedule(reminder.reminderId, reminder.remindAt)
        }
    }
}
