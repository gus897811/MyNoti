package org.eos.mynoti.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.applyAppSettings
import org.eos.mynoti.domain.model.isEffectivelyImportant

enum class HomeFilter(
    val label: String,
    val packageName: String? = null
) {
    ALL("All"),
    IMPORTANT("Important"),
    KAKAOTALK("KakaoTalk", AppPackages.KAKAOTALK),
    LEARNING_X("LearningX", AppPackages.LEARNING_X),
    HEY_YOUNG("HeyYoung", AppPackages.HEY_YOUNG)
}

data class HomeUiState(
    val notifications: List<Notification> = emptyList(),
    val settings: AppSettings = AppSettings.defaults(),
    val selectedFilter: HomeFilter = HomeFilter.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val visibleNotifications: List<Notification>
        get() {
            val filtered = notifications.applyAppSettings(settings)
            return when (selectedFilter) {
                HomeFilter.ALL -> filtered
                HomeFilter.IMPORTANT -> filtered.filter {
                    it.isEffectivelyImportant(settings.highlightKeywords)
                }
                HomeFilter.KAKAOTALK,
                HomeFilter.LEARNING_X,
                HomeFilter.HEY_YOUNG -> filtered.filter {
                    it.appPackageName == selectedFilter.packageName
                }
            }
        }

    fun isImportant(notification: Notification): Boolean {
        return notification.isEffectivelyImportant(settings.highlightKeywords)
    }
}

class HomeViewModel(
    private val notificationRepository: NotificationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(HomeFilter.ALL)
    private val notifications = MutableStateFlow<List<Notification>>(emptyList())
    private val loading = MutableStateFlow(true)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        notifications,
        settingsRepository.settings,
        selectedFilter,
        loading,
        errorMessage
    ) { items, settings, filter, isLoading, error ->
        HomeUiState(
            notifications = items,
            settings = settings,
            selectedFilter = filter,
            isLoading = isLoading,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        refresh()
    }

    fun onFilterSelected(filter: HomeFilter) {
        selectedFilter.value = filter
    }

    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            errorMessage.value = null
            runCatching { notificationRepository.getNotifications() }
                .onSuccess { notifications.value = it }
                .onFailure { errorMessage.value = it.message ?: "알림을 불러오지 못했습니다." }
            loading.value = false
        }
    }

    companion object {
        fun factory(
            notificationRepository: NotificationRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(notificationRepository, settingsRepository) as T
                }
            }
        }
    }
}
