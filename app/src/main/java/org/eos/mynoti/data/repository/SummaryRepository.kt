package org.eos.mynoti.data.repository

import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.domain.model.DailySummary

interface SummaryRepository {
    fun observeDailySummary(): Flow<DailySummary>

    suspend fun getDailySummary(): DailySummary
}
