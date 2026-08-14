package org.eos.mynoti.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eos.mynoti.data.InstalledAppCatalog
import org.eos.mynoti.data.InstalledAppInfo
import org.eos.mynoti.data.NotificationIngest
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.domain.model.AppSettings

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val installedAppCatalog: InstalledAppCatalog,
    private val notificationIngest: NotificationIngest? = null
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings.defaults()
    )

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _installedAppsLoading = MutableStateFlow(false)
    val installedAppsLoading: StateFlow<Boolean> = _installedAppsLoading.asStateFlow()

    private val _pickerQuery = MutableStateFlow("")
    val pickerQuery: StateFlow<String> = _pickerQuery.asStateFlow()

    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages.asSharedFlow()

    fun onTargetAppToggled(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTargetAppEnabled(packageName, enabled)
        }
    }

    fun addTargetApp(packageName: String, name: String) {
        viewModelScope.launch {
            settingsRepository.addTargetApp(packageName, name)
        }
    }

    fun removeTargetApp(packageName: String) {
        viewModelScope.launch {
            settingsRepository.removeTargetApp(packageName)
        }
    }

    fun setPickerQuery(query: String) {
        _pickerQuery.value = query
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedAppsLoading.value = true
            _installedApps.value = withContext(Dispatchers.IO) {
                installedAppCatalog.listLaunchableApps()
            }
            _installedAppsLoading.value = false
        }
    }

    fun addHighlightKeyword(keyword: String) {
        viewModelScope.launch {
            settingsRepository.addHighlightKeyword(keyword)
        }
    }

    fun removeHighlightKeyword(keyword: String) {
        viewModelScope.launch {
            settingsRepository.removeHighlightKeyword(keyword)
        }
    }

    fun addMuteKeyword(keyword: String) {
        viewModelScope.launch {
            settingsRepository.addMuteKeyword(keyword)
        }
    }

    fun removeMuteKeyword(keyword: String) {
        viewModelScope.launch {
            settingsRepository.removeMuteKeyword(keyword)
        }
    }

    fun addLearningXSample() {
        val ingest = notificationIngest ?: return
        viewModelScope.launch {
            ingest.insertLearningXSample()
            _messages.tryEmit(org.eos.mynoti.R.string.debug_sample_added)
        }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            installedAppCatalog: InstalledAppCatalog,
            notificationIngest: NotificationIngest? = null
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        settingsRepository,
                        installedAppCatalog,
                        notificationIngest
                    ) as T
                }
            }
        }
    }
}
