package org.eos.mynoti.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eos.mynoti.data.NotificationIngest
import org.eos.mynoti.data.datastore.AppCollectionStore
import org.eos.mynoti.data.local.AppDatabase
import org.eos.mynoti.data.local.DatabaseSeeder
import org.eos.mynoti.data.local.DefaultSettingsRepository
import org.eos.mynoti.data.local.RoomManualCalendarEventRepository
import org.eos.mynoti.data.local.RoomNotificationRepository
import org.eos.mynoti.data.local.RoomReminderRepository
import org.eos.mynoti.data.local.RoomSummaryRepository
import org.eos.mynoti.data.remote.NetworkModule
import org.eos.mynoti.data.remote.RemoteDataSource
import org.eos.mynoti.data.reminder.ReminderNotifier
import org.eos.mynoti.data.reminder.ReminderScheduler
import org.eos.mynoti.data.repository.DefaultLlmRepository
import org.eos.mynoti.data.repository.LlmRepository
import org.eos.mynoti.data.repository.ManualCalendarEventRepository
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.ReminderRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.data.repository.SummaryRepository
import org.eos.mynoti.data.work.AnalysisScheduler

class AppContainer(context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database: AppDatabase = AppDatabase.getInstance(context)
    private val remoteDataSource = RemoteDataSource(NetworkModule.createApiService())

    val notificationRepository: NotificationRepository =
        RoomNotificationRepository(database.notificationDao())

    val llmRepository: LlmRepository = DefaultLlmRepository(remoteDataSource)

    val settingsRepository: SettingsRepository = DefaultSettingsRepository(
        collectionStore = AppCollectionStore(context),
        keywordRuleDao = database.keywordRuleDao()
    )

    val summaryRepository: SummaryRepository = RoomSummaryRepository(
        notificationRepository = notificationRepository,
        settingsRepository = settingsRepository
    )

    val reminderRepository: ReminderRepository = RoomReminderRepository(
        reminderDao = database.reminderDao(),
        notificationDao = database.notificationDao(),
        scheduler = ReminderScheduler(context),
        notifier = ReminderNotifier(context)
    )

    val manualCalendarEventRepository: ManualCalendarEventRepository =
        RoomManualCalendarEventRepository(database.manualCalendarEventDao())

    val notificationIngest: NotificationIngest = NotificationIngest(
        context = context.applicationContext,
        notificationRepository = notificationRepository
    )

    init {
        AnalysisScheduler.enqueuePeriodic(context)
        applicationScope.launch {
            DatabaseSeeder.seedIfNeeded(
                notificationDao = database.notificationDao(),
                keywordRuleDao = database.keywordRuleDao()
            )
            reminderRepository.reschedulePending()
            AnalysisScheduler.enqueue(context)
        }
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer가 제공되지 않았습니다.")
}
