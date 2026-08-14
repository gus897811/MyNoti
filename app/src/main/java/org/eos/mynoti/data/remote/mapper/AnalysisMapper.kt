package org.eos.mynoti.data.remote.mapper

import org.eos.mynoti.data.remote.dto.AnalyzeNotificationRequest
import org.eos.mynoti.data.remote.dto.AnalyzeNotificationResponse
import org.eos.mynoti.data.remote.dto.BatchAnalyzeRequest
import org.eos.mynoti.data.remote.dto.BatchAnalyzeResponse
import org.eos.mynoti.data.remote.dto.BatchNotificationItemDto
import org.eos.mynoti.data.remote.dto.BatchResultItemDto
import org.eos.mynoti.domain.model.BatchAnalysisResult
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationAnalysis
import org.eos.mynoti.domain.model.NotificationType
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val seoul: ZoneId = ZoneId.of("Asia/Seoul")

fun LocalDateTime.toIsoOffsetDateTime(): String {
    return atZone(seoul).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

fun String.toSeoulLocalDateTime(): LocalDateTime? {
    return runCatching {
        OffsetDateTime.parse(this).atZoneSameInstant(seoul).toLocalDateTime()
    }.getOrNull() ?: runCatching {
        LocalDateTime.parse(this)
    }.getOrNull()
}

fun Notification.toAnalyzeRequest(): AnalyzeNotificationRequest {
    return AnalyzeNotificationRequest(
        appName = appName,
        packageName = appPackageName,
        title = originalTitle.ifBlank { title }.ifBlank { null },
        content = content.ifBlank { null },
        receivedAt = receivedAt.toIsoOffsetDateTime()
    )
}

fun Notification.toBatchItem(): BatchNotificationItemDto {
    return BatchNotificationItemDto(
        localId = id,
        appName = appName,
        packageName = appPackageName,
        title = originalTitle.ifBlank { title }.ifBlank { null },
        content = content.ifBlank { null },
        receivedAt = receivedAt.toIsoOffsetDateTime()
    )
}

fun List<Notification>.toBatchRequest(): BatchAnalyzeRequest {
    return BatchAnalyzeRequest(notifications = map { it.toBatchItem() })
}

fun AnalyzeNotificationResponse.toAnalysis(localId: Long): NotificationAnalysis {
    return NotificationAnalysis(
        localId = localId,
        title = title,
        summary = summary,
        isImportant = isImportant,
        type = type.toNotificationType(),
        actionRequired = actionRequired,
        deadline = deadline?.toSeoulLocalDateTime(),
        actions = actions,
        isFallback = isFallback
    )
}

fun BatchResultItemDto.toAnalysis(): NotificationAnalysis {
    return NotificationAnalysis(
        localId = localId,
        title = title,
        summary = summary,
        isImportant = isImportant,
        type = type.toNotificationType(),
        actionRequired = actionRequired,
        deadline = deadline?.toSeoulLocalDateTime(),
        actions = actions,
        isFallback = isFallback
    )
}

fun BatchAnalyzeResponse.toDomain(): BatchAnalysisResult {
    return BatchAnalysisResult(
        results = results.map { it.toAnalysis() },
        failedIds = failed.map { it.localId }
    )
}

private fun String.toNotificationType(): NotificationType {
    return runCatching { NotificationType.valueOf(this) }.getOrDefault(NotificationType.ETC)
}
