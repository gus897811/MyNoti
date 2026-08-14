package org.eos.mynoti.service

data class ConversationMessage(
    val sender: String,
    val text: String,
    val timestamp: Long
)

/**
 * 카카오톡 등 MessagingStyle 대화 알림에서
 * 새로 들어온 메시지만 골라 본문을 만든다.
 */
object ConversationNotificationFormatter {

    fun fingerprint(message: ConversationMessage): String {
        return "${message.timestamp}|${message.sender}|${message.text}"
    }

    fun selectNewMessages(
        messages: List<ConversationMessage>,
        previousFingerprint: String?
    ): List<ConversationMessage> {
        if (messages.isEmpty()) return emptyList()
        val last = messages.last()
        if (previousFingerprint == null) {
            return messages.takeLast(MAX_FIRST_CAPTURE)
        }
        if (fingerprint(last) == previousFingerprint) {
            return emptyList()
        }
        val previousIndex = messages.indexOfLast { fingerprint(it) == previousFingerprint }
        return if (previousIndex >= 0) {
            messages.drop(previousIndex + 1)
        } else {
            listOf(last)
        }
    }

    fun formatContent(
        messages: List<ConversationMessage>,
        conversationTitle: String
    ): String {
        return messages.joinToString("\n") { message ->
            when {
                message.sender.isBlank() || message.sender == conversationTitle -> message.text
                else -> "${message.sender}: ${message.text}"
            }
        }
    }

    fun resolveTitle(
        conversationTitle: String,
        extrasTitle: String,
        lastSender: String
    ): String {
        return conversationTitle.ifBlank { extrasTitle }.ifBlank { lastSender }
    }

    private const val MAX_FIRST_CAPTURE = 5
}
