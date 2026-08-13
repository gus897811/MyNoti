package org.eos.mynoti.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import org.eos.mynoti.data.datastore.DataStoreSettingsRepository
import org.eos.mynoti.data.mock.MockNotificationRepository
import org.eos.mynoti.data.mock.MockSummaryRepository
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.data.repository.SummaryRepository

class AppContainer(context: Context) {
    val notificationRepository: NotificationRepository = MockNotificationRepository()
    val settingsRepository: SettingsRepository = DataStoreSettingsRepository(context)
    val summaryRepository: SummaryRepository = MockSummaryRepository(
        notificationRepository = notificationRepository,
        settingsRepository = settingsRepository
    )
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer가 제공되지 않았습니다.")
}
