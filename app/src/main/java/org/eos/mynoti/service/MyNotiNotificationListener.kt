package org.eos.mynoti.service

import android.os.Build
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
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.domain.model.Notification as DomainNotification
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

/**
 * 알림을 수집해 Room에만 저장한다. HTTP/LLM 호출은 WorkManager가 담당한다.
 *
 * 카카오톡은 MessagingStyle을 쓰고 대화방마다 같은 notification key를 갱신한다.
 * 키 중복 스킵 대신 마지막 메시지 fingerprint로 새 메시지만 저장한다.
 */
class MyNotiNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val seoul: ZoneId = ZoneId.of("Asia/Seoul")
    private val lastFingerprintByKey = ConcurrentHashMap<String, String>()

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return

        val parsed = PostedNotificationParser.parse(sbn.notification) ?: return
        val previousFingerprint = lastFingerprintByKey[sbn.key]
        val title: String
        val content: String
        val fingerprint: String
        val eventTimeMillis: Long

        if (parsed.isMessagingStyle) {
            val newMessages = ConversationNotificationFormatter.selectNewMessages(
                messages = parsed.messages,
                previousFingerprint = previousFingerprint
            )
            if (newMessages.isEmpty()) return
            title = parsed.title
            content = ConversationNotificationFormatter.formatContent(newMessages, title)
            fingerprint = ConversationNotificationFormatter.fingerprint(newMessages.last())
            eventTimeMillis = newMessages.last().timestamp.takeIf { it > 0 } ?: sbn.postTime
        } else {
            if (parsed.title.isBlank() && parsed.content.isBlank()) return
            fingerprint = parsed.fingerprint
            if (fingerprint == previousFingerprint) return
            title = parsed.title
            content = parsed.content
            eventTimeMillis = sbn.postTime
        }

        if (title.isBlank() && content.isBlank()) return
        rememberFingerprint(sbn.key, fingerprint)

        val receivedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(eventTimeMillis),
            seoul
        )

        scope.launch {
            val app = application as? MyNotiApplication ?: return@launch
            val settings = app.container.settingsRepository.settings.first()
            if (sbn.packageName !in settings.enabledPackageNames) return@launch

            val label = runCatching {
                val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getApplicationInfo(
                        sbn.packageName,
                        android.content.pm.PackageManager.ApplicationInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getApplicationInfo(sbn.packageName, 0)
                }
                packageManager.getApplicationLabel(info).toString()
            }.getOrDefault(sbn.packageName)

            app.container.notificationIngest.insertAndEnqueue(
                DomainNotification(
                    id = 0,
                    appName = label,
                    appPackageName = sbn.packageName,
                    title = title.ifBlank { label },
                    content = content.ifBlank { title },
                    summary = null,
                    receivedAt = receivedAt,
                    isImportant = false,
                    type = initialType(sbn.packageName),
                    analysisStatus = AnalysisStatus.PENDING
                )
            )
        }
    }

    private fun rememberFingerprint(key: String, fingerprint: String) {
        lastFingerprintByKey[key] = fingerprint
        if (lastFingerprintByKey.size > MAX_TRACKED_KEYS) {
            lastFingerprintByKey.keys.take(lastFingerprintByKey.size - MAX_TRACKED_KEYS)
                .forEach { lastFingerprintByKey.remove(it) }
        }
    }

    private fun initialType(packageName: String): NotificationType {
        return if (packageName == AppPackages.KAKAOTALK) {
            NotificationType.COMMUNICATION
        } else {
            NotificationType.ETC
        }
    }

    companion object {
        private const val MAX_TRACKED_KEYS = 200
    }
}
