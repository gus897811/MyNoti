package org.eos.mynoti.data

import android.content.Context
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.work.AnalysisScheduler
import org.eos.mynoti.domain.model.AnalysisStatus
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationType
import java.time.LocalDateTime

/**
 * NotificationListenerService / 디버그 샘플 알림이 공통으로 사용하는 진입점.
 * HTTP는 호출하지 않고 Room insert 후 WorkManager만 예약한다.
 */
class NotificationIngest(
    private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    suspend fun insertAndEnqueue(notification: Notification): Long {
        val id = notificationRepository.insertNotification(
            notification.copy(
                id = 0,
                summary = null,
                analysisStatus = AnalysisStatus.PENDING,
                actions = emptyList()
            )
        )
        AnalysisScheduler.enqueue(context)
        return id
    }

    suspend fun insertLearningXSample(now: LocalDateTime = LocalDateTime.now()): Long {
        return insertAndEnqueue(learningXSample(now))
    }

    companion object {
        fun learningXSample(now: LocalDateTime = LocalDateTime.now()): Notification {
            return Notification(
                id = 0,
                appName = "LearningX Student",
                appPackageName = AppPackages.LEARNING_X,
                title = "운영체제 과제 제출 안내",
                content = "운영체제 과제 2를 8월 14일 23:59까지 제출하세요.",
                summary = null,
                receivedAt = now,
                isImportant = false,
                type = NotificationType.ETC,
                analysisStatus = AnalysisStatus.PENDING
            )
        }
    }
}
