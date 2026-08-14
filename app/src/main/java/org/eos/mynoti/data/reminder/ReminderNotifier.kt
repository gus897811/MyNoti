package org.eos.mynoti.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import org.eos.mynoti.MainActivity
import org.eos.mynoti.R
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.Reminder

class ReminderNotifier(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    fun show(reminder: Reminder, notification: Notification) {
        createChannel()
        val contentIntent = Intent(
            Intent.ACTION_VIEW,
            "mynoti://notification/${notification.id}".toUri(),
            appContext,
            MainActivity::class.java
        )
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            reminder.id.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body = notification.summary?.takeIf { it.isNotBlank() } ?: notification.content
        val posted = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(notification.title.ifBlank { notification.appName })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(NOTIFICATION_ID_OFFSET + reminder.id.toInt(), posted)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            appContext.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = appContext.getString(R.string.reminder_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "mynoti.reminder"
        private const val NOTIFICATION_ID_OFFSET = 10_000
    }
}
