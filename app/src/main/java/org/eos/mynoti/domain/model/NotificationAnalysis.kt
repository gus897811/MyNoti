package org.eos.mynoti.domain.model

import java.time.LocalDateTime

/**
 * LLM 분석 결과. 백엔드 README의 AnalyzeResponse와 동일한 필드.
 * 백엔드는 저장하지 않으며, 안드로이드가 Room에 반영한다.
 */
data class NotificationAnalysis(
    val localId: Long,
    val summary: String,
    val isImportant: Boolean,
    val type: NotificationType,
    val actionRequired: Boolean,
    val deadline: LocalDateTime?,
    val actions: List<String>,
    val isFallback: Boolean
)

data class BatchAnalysisResult(
    val results: List<NotificationAnalysis>,
    val failedIds: List<Long>
)
