package org.eos.mynoti.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.eos.mynoti.data.local.converter.StringListConverter
import org.eos.mynoti.data.local.dao.NotificationDao
import org.eos.mynoti.data.local.mapper.toDomain
import org.eos.mynoti.data.local.mapper.toEntity
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.domain.model.AnalysisStatus
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationAnalysis

class RoomNotificationRepository(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    private val actionsConverter = StringListConverter()

    override fun observeNotifications(): Flow<List<Notification>> {
        return notificationDao.observeAll().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeImportantNotifications(): Flow<List<Notification>> {
        return notificationDao.observeImportant().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeNotificationsByApp(packageName: String): Flow<List<Notification>> {
        return notificationDao.observeByPackageName(packageName)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeNotification(id: Long): Flow<Notification?> {
        return notificationDao.observeById(id).map { it?.toDomain() }
    }

    override suspend fun getNotifications(): List<Notification> {
        return observeNotifications().first()
    }

    override suspend fun getNotification(id: Long): Notification? {
        return notificationDao.getById(id)?.toDomain()
    }

    override suspend fun getImportantNotifications(): List<Notification> {
        return observeImportantNotifications().first()
    }

    override suspend fun insertNotification(notification: Notification): Long {
        return notificationDao.insert(notification.toEntity())
    }

    override suspend fun updateNotification(notification: Notification) {
        val createdAt = notificationDao.getById(notification.id)?.createdAt ?: notification.receivedAt
        notificationDao.update(notification.toEntity(createdAt))
    }

    override suspend fun deleteNotification(id: Long) {
        notificationDao.deleteById(id)
    }

    override suspend fun setImportant(id: Long, isImportant: Boolean) {
        notificationDao.updateImportance(id, isImportant)
    }

    override suspend fun getPendingAnalysis(limit: Int): List<Notification> {
        return notificationDao.getPendingAnalysis(limit).map { it.toDomain() }
    }

    override suspend fun getByAnalysisStatus(status: AnalysisStatus, limit: Int): List<Notification> {
        return notificationDao.getByAnalysisStatus(status, limit).map { it.toDomain() }
    }

    override suspend fun markAnalysisStatus(id: Long, status: AnalysisStatus) {
        notificationDao.updateAnalysisStatus(id, status)
    }

    override suspend fun resetStuckAnalysis() {
        notificationDao.resetStuckAnalysis()
    }

    override suspend fun applyAnalysis(analysis: NotificationAnalysis) {
        val existing = notificationDao.getById(analysis.localId) ?: return
        notificationDao.applyAnalysis(
            id = analysis.localId,
            title = analysis.title?.takeIf { it.isNotBlank() } ?: existing.title,
            summary = analysis.summary,
            isImportant = analysis.isImportant,
            type = analysis.type,
            deadline = analysis.deadline ?: existing.deadline,
            actionsJson = actionsConverter.fromList(analysis.actions),
            status = AnalysisStatus.COMPLETED
        )
    }
}
