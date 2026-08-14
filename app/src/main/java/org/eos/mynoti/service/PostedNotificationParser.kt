package org.eos.mynoti.service

import android.app.Notification
import android.os.Bundle
import androidx.core.app.NotificationCompat

data class ParsedPostedNotification(
    val title: String,
    val content: String,
    val messages: List<ConversationMessage> = emptyList(),
    val isMessagingStyle: Boolean = false,
    val lastMessageTimestamp: Long = 0L
) {
    val fingerprint: String
        get() = if (isMessagingStyle) {
            messages.lastOrNull()?.let(ConversationNotificationFormatter::fingerprint).orEmpty()
        } else {
            "$title|$content"
        }
}

/**
 * 일반 알림(EXTRA_TITLE/TEXT)과 MessagingStyle 대화 알림을 모두 파싱한다.
 * 카카오톡은 대화방마다 같은 notification key를 갱신하므로,
 * EXTRA_TEXT가 비어 있어도 Style/EXTRA_MESSAGES에서 메시지를 읽어야 한다.
 */
object PostedNotificationParser {

    fun parse(notification: Notification): ParsedPostedNotification? {
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return null
        if (notification.category == Notification.CATEGORY_CALL) return null

        val fromStyle = NotificationCompat.MessagingStyle
            .extractMessagingStyleFromNotification(notification)
            ?.let { style -> parseMessagingStyle(notification, style) }
        if (fromStyle != null) return fromStyle

        val fromExtras = parseMessagingExtras(notification)
        if (fromExtras != null) return fromExtras

        return parseGeneric(notification)
    }

    private fun parseMessagingStyle(
        notification: Notification,
        style: NotificationCompat.MessagingStyle
    ): ParsedPostedNotification? {
        val extras = notification.extras
        val conversationTitle = style.conversationTitle?.toString()?.trim().orEmpty()
        val extrasTitle = extrasTitle(extras)
        val messages = style.messages.mapNotNull { it.toConversationMessage() }
        if (messages.isEmpty()) {
            val generic = parseGeneric(notification)
            val title = ConversationNotificationFormatter.resolveTitle(
                conversationTitle = conversationTitle,
                extrasTitle = extrasTitle,
                lastSender = ""
            )
            val content = generic?.content.orEmpty()
            if (title.isBlank() && content.isBlank()) return null
            return ParsedPostedNotification(
                title = title.ifBlank { generic?.title.orEmpty() },
                content = content,
                isMessagingStyle = false
            )
        }
        val title = ConversationNotificationFormatter.resolveTitle(
            conversationTitle = conversationTitle,
            extrasTitle = extrasTitle,
            lastSender = messages.last().sender
        )
        return ParsedPostedNotification(
            title = title,
            content = ConversationNotificationFormatter.formatContent(messages, title),
            messages = messages,
            isMessagingStyle = true,
            lastMessageTimestamp = messages.last().timestamp
        )
    }

    private fun parseMessagingExtras(notification: Notification): ParsedPostedNotification? {
        val extras = notification.extras ?: return null
        val parcels = messageParcels(extras) ?: return null
        val messages = parcels.mapNotNull { parcelable ->
            (parcelable as? Bundle)?.toConversationMessage()
        }
        if (messages.isEmpty()) return null

        val title = ConversationNotificationFormatter.resolveTitle(
            conversationTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
                ?.toString()?.trim().orEmpty(),
            extrasTitle = extrasTitle(extras),
            lastSender = messages.last().sender
        )
        return ParsedPostedNotification(
            title = title,
            content = ConversationNotificationFormatter.formatContent(messages, title),
            messages = messages,
            isMessagingStyle = true,
            lastMessageTimestamp = messages.last().timestamp
        )
    }

    private fun parseGeneric(notification: Notification): ParsedPostedNotification? {
        val extras = notification.extras ?: return null
        val title = extrasTitle(extras)
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }
            .orEmpty()
        val parts = buildList {
            addIfNotBlank(extras.getCharSequence(Notification.EXTRA_TEXT))
            addIfNotBlank(extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
            addIfNotBlank(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))
            addIfNotBlank(extras.getCharSequence(Notification.EXTRA_INFO_TEXT))
            addAll(lines)
        }.distinct()
        val content = parts.joinToString("\n")
        if (title.isBlank() && content.isBlank()) return null
        return ParsedPostedNotification(title = title, content = content)
    }

    private fun extrasTitle(extras: Bundle): String {
        return extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
            ?.toString()?.trim().orEmpty()
            .ifBlank {
                extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
            }
            .ifBlank {
                extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()?.trim().orEmpty()
            }
    }

    private fun NotificationCompat.MessagingStyle.Message.toConversationMessage(): ConversationMessage? {
        val sender = person?.name?.toString()?.trim().orEmpty()
        val text = text?.toString()?.trim().orEmpty().ifBlank { attachmentLabel(dataMimeType) }
        if (text.isBlank()) return null
        return ConversationMessage(sender = sender, text = text, timestamp = timestamp)
    }

    private fun Bundle.toConversationMessage(): ConversationMessage? {
        val text = getCharSequence(MESSAGE_KEY_TEXT)?.toString()?.trim().orEmpty()
            .ifBlank { attachmentLabel(getString(MESSAGE_KEY_MIME_TYPE)) }
        if (text.isBlank()) return null
        val sender = getCharSequence(MESSAGE_KEY_SENDER)?.toString()?.trim().orEmpty()
        val timestamp = getLong(MESSAGE_KEY_TIMESTAMP, 0L)
        return ConversationMessage(sender = sender, text = text, timestamp = timestamp)
    }

    private fun messageParcels(extras: Bundle): Array<*>? {
        @Suppress("DEPRECATION")
        return extras.getParcelableArray(Notification.EXTRA_MESSAGES)?.takeIf { it.isNotEmpty() }
    }

    private fun MutableList<String>.addIfNotBlank(value: CharSequence?) {
        val text = value?.toString()?.trim().orEmpty()
        if (text.isNotBlank()) add(text)
    }

    private fun attachmentLabel(mimeType: String?): String {
        val mime = mimeType.orEmpty()
        return when {
            mime.startsWith("image/") -> "사진"
            mime.startsWith("video/") -> "동영상"
            mime.startsWith("audio/") -> "음성"
            mime.isNotBlank() -> "첨부파일"
            else -> ""
        }
    }

    private const val MESSAGE_KEY_TEXT = "text"
    private const val MESSAGE_KEY_TIMESTAMP = "time"
    private const val MESSAGE_KEY_SENDER = "sender"
    private const val MESSAGE_KEY_MIME_TYPE = "type"
}
