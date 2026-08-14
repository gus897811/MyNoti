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
            type = NotificationType.ETC,
            remindAt = null,
            isReminded = false
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
    }
}
