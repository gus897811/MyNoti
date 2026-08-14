package org.eos.mynoti

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

object NotificationParser {
    const val KAKAOTALK_PACKAGE = "com.kakao.talk"

    fun parse(sbn: StatusBarNotification, appLabel: String): List<CapturedNotification> {
        if (shouldSkip(sbn)) return emptyList()

        val messaging = parseMessagingStyle(sbn, appLabel)
        if (messaging.isNotEmpty()) return messaging

        val inboxLines = parseInboxStyle(sbn, appLabel)
        if (inboxLines.isNotEmpty()) return inboxLines

        val extras = sbn.notification.extras
        val title = firstNonBlank(
            extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE),
            extras.getCharSequence(Notification.EXTRA_TITLE),
            extras.getCharSequence(Notification.EXTRA_TITLE_BIG),
        ) ?: appLabel
        val text = firstNonBlank(
            extras.getCharSequence(Notification.EXTRA_TEXT),
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT),
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT),
            extras.getCharSequence(Notification.EXTRA_INFO_TEXT),
            sbn.notification.tickerText,
        ).orEmpty()

        if (title.isBlank() && text.isBlank()) return emptyList()

        return listOf(
            captured(
                sbn = sbn,
                key = sbn.key,
                appLabel = appLabel,
                title = title.ifBlank { "(제목 없음)" },
                text = text,
                postedAtMillis = sbn.postTime,
            ),
        )
    }

    private fun shouldSkip(sbn: StatusBarNotification): Boolean {
        val flags = sbn.notification.flags
        if (flags and Notification.FLAG_FOREGROUND_SERVICE != 0) return true
        if (flags and Notification.FLAG_GROUP_SUMMARY == 0) return false
        val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(sbn.notification)
        return style?.messages.isNullOrEmpty() && extractRawMessages(sbn.notification).isEmpty()
    }

    private fun parseMessagingStyle(
        sbn: StatusBarNotification,
        appLabel: String,
    ): List<CapturedNotification> {
        val notification = sbn.notification
        val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        val conversation = firstNonBlank(
            style?.conversationTitle,
            notification.extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE),
            notification.extras.getCharSequence(Notification.EXTRA_TITLE),
        )

        val messages = style?.messages?.map { message ->
            RawMessage(
                sender = messageSender(message),
                text = messageText(message),
                timestamp = message.timestamp,
            )
        }.orEmpty().ifEmpty { extractRawMessages(notification) }

        return messages.mapNotNull { message ->
            val text = message.text
            if (text.isBlank()) return@mapNotNull null
            val sender = message.sender
            val title = conversation ?: sender.ifBlank { appLabel }
            val body = if (!conversation.isNullOrBlank() && sender.isNotBlank() && sender != conversation) {
                "$sender: $text"
            } else {
                text
            }
            val time = message.timestamp.takeIf { it > 0L } ?: sbn.postTime
            captured(
                sbn = sbn,
                key = "${sbn.key}|$time|$sender|$text",
                appLabel = appLabel,
                title = title.ifBlank { "(제목 없음)" },
                text = body,
                postedAtMillis = time,
            )
        }
    }

    private fun parseInboxStyle(
        sbn: StatusBarNotification,
        appLabel: String,
    ): List<CapturedNotification> {
        val lines = sbn.notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }
            .orEmpty()
        if (lines.isEmpty()) return emptyList()

        val title = firstNonBlank(
            sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE),
            sbn.notification.extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE),
        ) ?: appLabel

        return lines.mapIndexed { index, line ->
            captured(
                sbn = sbn,
                key = "${sbn.key}|inbox|$index|$line",
                appLabel = appLabel,
                title = title,
                text = line,
                postedAtMillis = sbn.postTime,
            )
        }
    }

    private fun extractRawMessages(notification: Notification): List<RawMessage> {
        val parcels = notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return emptyList()
        return parcels.mapNotNull { parcelable ->
            val bundle = parcelable as? Bundle ?: return@mapNotNull null
            val text = bundle.getCharSequence("text")?.toString().orEmpty()
            if (text.isBlank()) return@mapNotNull null
            RawMessage(
                sender = bundle.getCharSequence("sender")?.toString().orEmpty(),
                text = text,
                timestamp = bundle.getLong("time"),
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun messageSender(message: NotificationCompat.MessagingStyle.Message): String {
        message.person?.name?.toString()?.takeIf { it.isNotBlank() }?.let { return it }
        return message.sender?.toString().orEmpty()
    }

    private fun messageText(message: NotificationCompat.MessagingStyle.Message): String {
        val text = message.text?.toString().orEmpty()
        if (text.isNotBlank()) return text
        val mime = message.dataMimeType.orEmpty()
        return when {
            mime.startsWith("image/") -> "[사진]"
            mime.startsWith("video/") -> "[동영상]"
            mime.startsWith("audio/") -> "[음성]"
            mime.isNotBlank() -> "[첨부파일]"
            else -> ""
        }
    }

    private fun captured(
        sbn: StatusBarNotification,
        key: String,
        appLabel: String,
        title: String,
        text: String,
        postedAtMillis: Long,
    ): CapturedNotification {
        return CapturedNotification(
            key = key,
            notificationKey = sbn.key,
            packageName = sbn.packageName,
            appLabel = appLabel,
            title = title,
            text = text,
            postedAtMillis = postedAtMillis,
            isKakaoTalk = sbn.packageName == KAKAOTALK_PACKAGE,
        )
    }

    private fun firstNonBlank(vararg values: CharSequence?): String? {
        return values.firstNotNullOfOrNull { value ->
            value?.toString()?.trim()?.takeIf { it.isNotBlank() }
        }
    }

    private data class RawMessage(
        val sender: String,
        val text: String,
        val timestamp: Long,
    )
}
