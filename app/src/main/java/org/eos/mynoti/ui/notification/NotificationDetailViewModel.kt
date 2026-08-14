package org.eos.mynoti.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.ReminderRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.Reminder
import org.eos.mynoti.domain.model.isEffectivelyImportant
import java.time.LocalDateTime

data class NotificationDetailUiState(
    val notification: Notification? = null,
    val isImportant: Boolean = false,
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true
)

class NotificationDetailViewModel(
    private val notificationId: Long,
    private val repository: NotificationRepository,
    settingsRepository: SettingsRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<NotificationDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<NotificationDetailEvent> = _events.asSharedFlow()

    val uiState: StateFlow<NotificationDetailUiState> = combine(
        repository.observeNotification(notificationId),
        settingsRepository.settings,
        reminderRepository.observeByNotificationId(notificationId)
    ) { notification, settings, reminders ->
        NotificationDetailUiState(
            notification = notification,
            isImportant = notification?.isEffectivelyImportant(settings.highlightKeywords) == true,
            reminders = reminders,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotificationDetailUiState()
    )

    fun toggleImportant() {
        val current = uiState.value.notification ?: return
        viewModelScope.launch {
            repository.setImportant(current.id, !current.isImportant)
        }
    }

    fun scheduleReminder(remindAt: LocalDateTime) {
        viewModelScope.launch {
            reminderRepository.schedule(notificationId, remindAt)
        }
    }

    fun cancelReminder(reminderId: Long) {
        viewModelScope.launch {
            reminderRepository.cancel(reminderId)
        }
    }

    fun deleteNotification() {
        viewModelScope.launch {
            uiState.value.reminders.forEach { reminder ->
                reminderRepository.cancel(reminder.id)
            }
            repository.deleteNotification(notificationId)
            _events.emit(NotificationDetailEvent.Deleted)
        }
    }

    companion object {
        fun factory(
            notificationId: Long,
            repository: NotificationRepository,
            settingsRepository: SettingsRepository,
            reminderRepository: ReminderRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NotificationDetailViewModel(
                        notificationId,
                        repository,
                        settingsRepository,
                        reminderRepository
                    ) as T
                }
            }
        }
    }
}

sealed interface NotificationDetailEvent {
    data object Deleted : NotificationDetailEvent
}
