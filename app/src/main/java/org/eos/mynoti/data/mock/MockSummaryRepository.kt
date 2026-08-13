package org.eos.mynoti.data.mock

import kotlinx.coroutines.flow.first
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.data.repository.SummaryRepository
import org.eos.mynoti.domain.model.DailySummary
import org.eos.mynoti.domain.model.DailySummaryFactory

class MockSummaryRepository(
    private val notificationRepository: NotificationRepository,
    private val settingsRepository: SettingsRepository
) : SummaryRepository {

    override suspend fun getDailySummary(): DailySummary {
        val notifications = notificationRepository.getNotifications()
        val settings = settingsRepository.settings.first()
        return DailySummaryFactory.create(notifications, settings)
    }
}
