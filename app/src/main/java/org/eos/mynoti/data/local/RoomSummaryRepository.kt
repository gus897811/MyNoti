package org.eos.mynoti.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.data.repository.SummaryRepository
import org.eos.mynoti.domain.model.DailySummary
import org.eos.mynoti.domain.model.DailySummaryFactory

class RoomSummaryRepository(
    private val notificationRepository: NotificationRepository,
    private val settingsRepository: SettingsRepository
) : SummaryRepository {

    override fun observeDailySummary(): Flow<DailySummary> {
        return combine(
            notificationRepository.observeNotifications(),
            settingsRepository.settings
        ) { notifications, settings ->
            DailySummaryFactory.create(notifications, settings)
        }
    }

    override suspend fun getDailySummary(): DailySummary {
        return observeDailySummary().first()
    }
}
