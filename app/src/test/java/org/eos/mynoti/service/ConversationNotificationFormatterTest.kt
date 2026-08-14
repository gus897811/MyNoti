package org.eos.mynoti.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationNotificationFormatterTest {

    @Test
    fun firstCaptureTakesRecentMessagesOnly() {
        val selected = ConversationNotificationFormatter.selectNewMessages(
            messages = messages("안녕", "과제 마감 언제야", "내일 23:59"),
            previousFingerprint = null
        )
        assertEquals(listOf("안녕", "과제 마감 언제야", "내일 23:59"), selected.map { it.text })
    }

    @Test
    fun sameLastMessageIsIgnored() {
        val all = messages("안녕", "내일 도서관")
        val previous = ConversationNotificationFormatter.fingerprint(all.last())
        val selected = ConversationNotificationFormatter.selectNewMessages(all, previous)
        assertTrue(selected.isEmpty())
    }

    @Test
    fun onlyMessagesAfterPreviousFingerprintAreKept() {
        val all = messages("안녕", "내일 도서관", "3시에 보자")
        val previous = ConversationNotificationFormatter.fingerprint(all[1])
        val selected = ConversationNotificationFormatter.selectNewMessages(all, previous)
        assertEquals(listOf("3시에 보자"), selected.map { it.text })
    }

    @Test
    fun unknownPreviousFingerprintFallsBackToLastMessage() {
        val all = messages("오래된 메시지", "새 메시지")
        val selected = ConversationNotificationFormatter.selectNewMessages(all, "0|ghost|없음")
        assertEquals(listOf("새 메시지"), selected.map { it.text })
    }

    @Test
    fun groupChatPrefixesSenderWhenDifferentFromTitle() {
        val content = ConversationNotificationFormatter.formatContent(
            messages = listOf(
                ConversationMessage("민준", "초안 공유해줘", 1),
                ConversationMessage("수아", "알겠어", 2)
            ),
            conversationTitle = "캡스톤 팀플방"
        )
        assertEquals("민준: 초안 공유해줘\n수아: 알겠어", content)
    }

    @Test
    fun oneToOneOmitsSenderWhenItMatchesTitle() {
        val content = ConversationNotificationFormatter.formatContent(
            messages = listOf(ConversationMessage("수아", "학식 갈래?", 1)),
            conversationTitle = "수아"
        )
        assertEquals("학식 갈래?", content)
    }

    private fun messages(vararg texts: String): List<ConversationMessage> {
        return texts.mapIndexed { index, text ->
            ConversationMessage(sender = "민준", text = text, timestamp = (index + 1) * 1_000L)
        }
    }
}
