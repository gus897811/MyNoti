package org.eos.mynoti.data.repository

import org.eos.mynoti.domain.model.BatchAnalysisResult
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationAnalysis

interface LlmRepository {
    suspend fun analyze(notification: Notification): NotificationAnalysis?

    suspend fun analyzeBatch(notifications: List<Notification>): BatchAnalysisResult
}
