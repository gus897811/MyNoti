package org.eos.mynoti.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    notificationRepository: NotificationRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(HomeFilter.ALL)

    val uiState: StateFlow<HomeUiState> = combine(
        notificationRepository.observeNotifications(),
        settingsRepository.settings,
        selectedFilter
    ) { items, settings, filter ->
        HomeUiState(
            notifications = items,
            settings = settings,
            selectedFilter = filter,
            isLoading = false,
            errorMessage = null
        )
    }.catch { error ->
        emit(
            HomeUiState(
                isLoading = false,
                errorMessage = error.message ?: "알림을 불러오지 못했습니다."
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun onFilterSelected(filter: HomeFilter) {
        selectedFilter.value = filter
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
