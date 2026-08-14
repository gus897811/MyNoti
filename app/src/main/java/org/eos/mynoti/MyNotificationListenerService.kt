package org.eos.mynoti

import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MyNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeNotifications.orEmpty().forEach { statusBarNotification ->
            capture(statusBarNotification)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        capture(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationRepository.markRemoved(sbn.key)
    }

    private fun capture(sbn: StatusBarNotification) {
        NotificationRepository.addAll(NotificationParser.parse(sbn, labelFor(sbn.packageName)))
    }

    private fun labelFor(packageName: String): String {
        if (packageName == NotificationParser.KAKAOTALK_PACKAGE) return "카카오톡"
        if (packageName == this.packageName) return "MyNoti"
        return runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA),
            ).toString()
        }.getOrDefault(packageName)
    }
}
