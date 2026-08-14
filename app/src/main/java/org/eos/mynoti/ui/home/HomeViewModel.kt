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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationFilter
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.domain.model.TargetApp
import org.eos.mynoti.domain.model.applyAppSettings
import org.eos.mynoti.domain.model.applyFilter
import org.eos.mynoti.domain.model.isEffectivelyImportant

data class HomeUiState(
    val visibleNotifications: List<Notification> = emptyList(),
    val settings: AppSettings = AppSettings.defaults(),
    val filter: NotificationFilter = NotificationFilter(),
    val filterApps: List<TargetApp> = AppSettings.defaultTargetApps,
    val filtersExpanded: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    fun isImportant(notification: Notification): Boolean {
        return notification.isEffectivelyImportant(settings.highlightKeywords)
    }
}

class HomeViewModel(
    notificationRepository: NotificationRepository,
    settingsRepository: SettingsRepository,
    private val homeFilterController: HomeFilterController = HomeFilterController()
) : ViewModel() {

    private val filter = MutableStateFlow(NotificationFilter())
    private val filtersExpanded = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            homeFilterController.pending.collect { preset ->
                if (preset != null) {
                    applyPreset(preset)
                    homeFilterController.consume()
                }
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        notificationRepository.observeNotifications(),
        settingsRepository.settings,
        filter,
        filtersExpanded
    ) { items, settings, currentFilter, expanded ->
        val validPackages = settings.targetApps.map { it.packageName }.toSet()
        val prunedFilter = currentFilter.pruneSelectedApps(validPackages)
        val visible = items
            .applyAppSettings(settings)
            .applyFilter(prunedFilter)
        HomeUiState(
            visibleNotifications = visible,
            settings = settings,
            filter = prunedFilter,
            filterApps = settings.targetApps,
            filtersExpanded = expanded,
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

    fun selectAllApps() {
        filter.update { it.copy(selectedApps = emptySet()) }
    }

    fun toggleApp(packageName: String) {
        filter.update { current ->
            val selected = if (packageName in current.selectedApps) {
                current.selectedApps - packageName
            } else {
                current.selectedApps + packageName
            }
            current.copy(selectedApps = selected)
        }
    }

    fun removeAppFilter(packageName: String) {
        filter.update { current ->
            current.copy(selectedApps = current.selectedApps - packageName)
        }
    }

    fun toggleType(type: NotificationType) {
        filter.update { current ->
            val selected = if (type in current.selectedTypes) {
                current.selectedTypes - type
            } else {
                current.selectedTypes + type
            }
            current.copy(selectedTypes = selected)
        }
    }

    fun toggleImportant() {
        filter.update { it.copy(importantOnly = !it.importantOnly) }
    }

    fun clearFilters() {
        filter.value = NotificationFilter()
    }

    fun toggleFiltersExpanded() {
        filtersExpanded.update { !it }
    }

    fun applyPreset(preset: HomeFilterPreset) {
        filter.value = preset.toFilter()
        filtersExpanded.value = true
    }

    companion object {
        fun factory(
            notificationRepository: NotificationRepository,
            settingsRepository: SettingsRepository,
            homeFilterController: HomeFilterController = HomeFilterController()
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        notificationRepository,
                        settingsRepository,
                        homeFilterController
                    ) as T
                }
            }
        }
    }
}
