package org.eos.mynoti.data.repository

import org.eos.mynoti.data.remote.RemoteDataSource
import org.eos.mynoti.data.remote.mapper.toAnalysisOrNull
import org.eos.mynoti.data.remote.mapper.toAnalyzeRequest
import org.eos.mynoti.data.remote.mapper.toBatchRequest
import org.eos.mynoti.data.remote.mapper.toDomain
import org.eos.mynoti.domain.model.BatchAnalysisResult
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationAnalysis

class DefaultLlmRepository(
    private val remoteDataSource: RemoteDataSource
) : LlmRepository {

    override suspend fun analyze(notification: Notification): NotificationAnalysis? {
        return remoteDataSource.analyze(notification.toAnalyzeRequest())
            .toAnalysisOrNull(notification.id)
    }

    override suspend fun analyzeBatch(notifications: List<Notification>): BatchAnalysisResult {
        if (notifications.isEmpty()) {
            return BatchAnalysisResult(
                results = emptyList(),
                failedIds = emptyList(),
                filteredIds = emptyList()
            )
        }
        return remoteDataSource.analyzeBatch(notifications.toBatchRequest()).toDomain()
    }
}
