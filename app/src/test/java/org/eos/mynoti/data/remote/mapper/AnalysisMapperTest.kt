package org.eos.mynoti.data.remote.mapper

import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class AnalysisMapperTest {

    @Test
    fun toAnalyzeRequest_usesCamelCaseFieldsAndSeoulOffset() {
        val notification = Notification(
            id = 101,
            appName = "LearningX Student",
            appPackageName = AppPackages.LEARNING_X,
            title = "운영체제 과제 제출 안내",
            content = "운영체제 과제 2를 8월 14일 23:59까지 제출하세요.",
            summary = null,
            receivedAt = LocalDateTime.of(2026, 8, 13, 10, 30),
            isImportant = false,
            type = NotificationType.ETC
        )

        val request = notification.toAnalyzeRequest()
        assertEquals("LearningX Student", request.appName)
        assertEquals(AppPackages.LEARNING_X, request.packageName)
        assertEquals("운영체제 과제 제출 안내", request.title)
        assertEquals("운영체제 과제 2를 8월 14일 23:59까지 제출하세요.", request.content)
        assertEquals("2026-08-13T10:30:00+09:00", request.receivedAt)
    }

    @Test
    fun deadlineParsesIsoOffsetToSeoulLocalDateTime() {
        val parsed = "2026-08-14T23:59:00+09:00".toSeoulLocalDateTime()
        assertEquals(LocalDateTime.of(2026, 8, 14, 23, 59), parsed)
    }

    @Test
    fun unknownTypeFallsBackToEtc() {
        val response = org.eos.mynoti.data.remote.dto.AnalyzeNotificationResponse(
            summary = "요약",
            isImportant = true,
            type = "UNKNOWN",
            actionRequired = false,
            deadline = null,
            actions = emptyList()
        )
        val analysis = response.toAnalysis(localId = 1)
        assertEquals(NotificationType.ETC, analysis.type)
        assertNull(analysis.deadline)
        assertNull(analysis.title)
    }

    @Test
    fun toAnalysis_mapsAiTitle() {
        val response = org.eos.mynoti.data.remote.dto.AnalyzeNotificationResponse(
            title = "운영체제 과제 2 제출",
            summary = "요약",
            isImportant = true,
            type = "ASSIGNMENT",
            actionRequired = true,
            deadline = null,
            actions = emptyList()
        )
        val analysis = response.toAnalysis(localId = 7)
        assertEquals("운영체제 과제 2 제출", analysis.title)
        assertEquals(7L, analysis.localId)
    }

    @Test
    fun toAnalyzeRequest_sendsOriginalTitleNotAiTitle() {
        val notification = Notification(
            id = 101,
            appName = "KakaoTalk",
            appPackageName = AppPackages.KAKAOTALK,
            title = "수아와의 점심 약속",
            originalTitle = "수아",
            content = "내일 점심 어때?",
            summary = "수아와 내일 점심 약속을 잡았습니다.",
            receivedAt = LocalDateTime.of(2026, 8, 13, 10, 30),
            isImportant = false,
            type = NotificationType.COMMUNICATION
        )

        val request = notification.toAnalyzeRequest()
        assertEquals("수아", request.title)
    }
}
