package org.eos.mynoti

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MyNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeNotifications.orEmpty().forEach { statusBarNotification ->
            NotificationRepository.add(statusBarNotification.toCaptured())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationRepository.add(sbn.toCaptured())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationRepository.markRemoved(sbn.key)
    }

    private fun StatusBarNotification.toCaptured(): CapturedNotification {
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: ""
        return CapturedNotification(
            key = key,
            packageName = packageName,
            title = title.ifBlank { "(제목 없음)" },
            text = text,
            postedAtMillis = postTime,
        )
    }
}
