package org.eos.mynoti.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.isEffectivelyImportant

data class NotificationDetailUiState(
    val notification: Notification? = null,
    val isImportant: Boolean = false,
    val isLoading: Boolean = true
)

class NotificationDetailViewModel(
    private val notificationId: Long,
    private val repository: NotificationRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<NotificationDetailUiState> = combine(
        repository.observeNotification(notificationId),
        settingsRepository.settings
    ) { notification, settings ->
        NotificationDetailUiState(
            notification = notification,
            isImportant = notification?.isEffectivelyImportant(settings.highlightKeywords) == true,
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

    companion object {
        fun factory(
            notificationId: Long,
            repository: NotificationRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NotificationDetailViewModel(
                        notificationId,
                        repository,
                        settingsRepository
                    ) as T
                }
            }
        }
    }
}
