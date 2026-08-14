package org.eos.mynoti.data.mock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.domain.model.AnalysisStatus
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationAnalysis
import java.time.LocalDateTime

class MockNotificationRepository(
    now: LocalDateTime = LocalDateTime.now()
) : NotificationRepository {

    private val notifications = MutableStateFlow(MockNotificationData.create(now))

    override fun observeNotifications(): Flow<List<Notification>> {
        return notifications.map { items -> items.sortedByDescending { it.receivedAt } }
    }

    override fun observeImportantNotifications(): Flow<List<Notification>> {
        return notifications.map { items ->
            items.filter { it.isImportant }.sortedByDescending { it.receivedAt }
        }
    }

    override fun observeNotificationsByApp(packageName: String): Flow<List<Notification>> {
        return notifications.map { items ->
            items.filter { it.appPackageName == packageName }.sortedByDescending { it.receivedAt }
        }
    }

    override fun observeNotification(id: Long): Flow<Notification?> {
        return notifications.map { items -> items.find { it.id == id } }
    }

    override suspend fun getNotifications(): List<Notification> {
        return notifications.value.sortedByDescending { it.receivedAt }
    }

    override suspend fun getNotification(id: Long): Notification? {
        return notifications.value.find { it.id == id }
    }

    override suspend fun getImportantNotifications(): List<Notification> {
        return notifications.value
            .filter { it.isImportant }
            .sortedByDescending { it.receivedAt }
    }

    override suspend fun insertNotification(notification: Notification): Long {
        val id = if (notification.id == 0L) {
            (notifications.value.maxOfOrNull { it.id } ?: 0L) + 1L
        } else {
            notification.id
        }
        notifications.update { current ->
            current.filterNot { it.id == id } + notification.copy(id = id)
        }
        return id
    }

    override suspend fun updateNotification(notification: Notification) {
        notifications.update { current ->
            current.map { if (it.id == notification.id) notification else it }
        }
    }

    override suspend fun deleteNotification(id: Long) {
        notifications.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun setImportant(id: Long, isImportant: Boolean) {
        notifications.update { current ->
            current.map { if (it.id == id) it.copy(isImportant = isImportant) else it }
        }
    }

    override suspend fun getPendingAnalysis(limit: Int): List<Notification> {
        return notifications.value
            .filter {
                it.analysisStatus == AnalysisStatus.PENDING ||
                    it.analysisStatus == AnalysisStatus.FAILED
            }
            .sortedBy { it.receivedAt }
            .take(limit)
    }

    override suspend fun markAnalysisStatus(id: Long, status: AnalysisStatus) {
        notifications.update { current ->
            current.map { if (it.id == id) it.copy(analysisStatus = status) else it }
        }
    }

    override suspend fun resetStuckAnalysis() {
        notifications.update { current ->
            current.map {
                if (it.analysisStatus == AnalysisStatus.IN_PROGRESS) {
                    it.copy(analysisStatus = AnalysisStatus.PENDING)
                } else {
                    it
                }
            }
        }
    }

    override suspend fun applyAnalysis(analysis: NotificationAnalysis) {
        notifications.update { current ->
            current.map { notification ->
                if (notification.id != analysis.localId) {
                    notification
                } else {
                    notification.copy(
                        summary = analysis.summary,
                        isImportant = analysis.isImportant,
                        type = analysis.type,
                        deadline = analysis.deadline ?: notification.deadline,
                        actions = analysis.actions.mapIndexed { index, title ->
                            org.eos.mynoti.domain.model.NotificationAction(
                                id = index.toLong(),
                                title = title
                            )
                        },
                        analysisStatus = AnalysisStatus.COMPLETED
                    )
                }
            }
        }
    }
}
