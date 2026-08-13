package org.eos.mynoti.data.repository

import org.eos.mynoti.domain.model.DailySummary

interface SummaryRepository {
    suspend fun getDailySummary(): DailySummary
}
