package org.eos.mynoti.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eos.mynoti.data.datastore.AppCollectionStore
import org.eos.mynoti.data.local.AppDatabase
import org.eos.mynoti.data.local.DatabaseSeeder
import org.eos.mynoti.data.local.DefaultSettingsRepository
import org.eos.mynoti.data.local.RoomNotificationRepository
import org.eos.mynoti.data.local.RoomSummaryRepository
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.data.repository.SummaryRepository

class AppContainer(context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database: AppDatabase = AppDatabase.getInstance(context)

    val notificationRepository: NotificationRepository =
        RoomNotificationRepository(database.notificationDao())

    val settingsRepository: SettingsRepository = DefaultSettingsRepository(
        collectionStore = AppCollectionStore(context),
        keywordRuleDao = database.keywordRuleDao()
    )

    val summaryRepository: SummaryRepository = RoomSummaryRepository(
        notificationRepository = notificationRepository,
        settingsRepository = settingsRepository
    )

    init {
        applicationScope.launch {
            DatabaseSeeder.seedIfNeeded(
                notificationDao = database.notificationDao(),
                keywordRuleDao = database.keywordRuleDao()
            )
        }
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer가 제공되지 않았습니다.")
}
