package org.eos.mynoti.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.eos.mynoti.MyNotiApplication
import org.eos.mynoti.domain.model.AnalysisStatus
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.domain.model.Notification as DomainNotification
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 알림을 수집해 Room에만 저장한다. HTTP/LLM 호출은 WorkManager가 담당한다.
 */
class MyNotiNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val seoul: ZoneId = ZoneId.of("Asia/Seoul")
    private var lastPostedKey: String? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (sbn.key == lastPostedKey) return
        lastPostedKey = sbn.key

        val title = sbn.notification.extras
            ?.getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()
            .orEmpty()
        val content = listOf(
            sbn.notification.extras?.getCharSequence(Notification.EXTRA_TEXT),
            sbn.notification.extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)
        ).mapNotNull { it?.toString()?.takeIf(String::isNotBlank) }
            .distinct()
            .joinToString("\n")

        if (title.isBlank() && content.isBlank()) return

        val receivedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(sbn.postTime),
            seoul
        )
        val appName = sbn.notification.extras
            ?.getCharSequence(Notification.EXTRA_SUB_TEXT)
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: sbn.packageName

        scope.launch {
            val app = application as? MyNotiApplication ?: return@launch
            val settings = app.container.settingsRepository.settings.first()
            if (sbn.packageName !in settings.enabledPackageNames) return@launch

            val label = runCatching {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(sbn.packageName, 0)
                ).toString()
            }.getOrDefault(appName)

            app.container.notificationIngest.insertAndEnqueue(
                DomainNotification(
                    id = 0,
                    appName = label,
                    appPackageName = sbn.packageName,
                    title = title.ifBlank { label },
                    content = content,
                    summary = null,
                    receivedAt = receivedAt,
                    isImportant = false,
                    type = NotificationType.ETC,
                    remindAt = null,
                    isReminded = false,
                    analysisStatus = AnalysisStatus.PENDING
                )
            )
        }
    }
}
