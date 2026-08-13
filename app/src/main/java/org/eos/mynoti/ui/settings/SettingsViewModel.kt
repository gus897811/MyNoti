package org.eos.mynoti.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.domain.model.AppSettings

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings.defaults()
    )

    fun onTargetAppToggled(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTargetAppEnabled(packageName, enabled)
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

    companion object {
        fun factory(settingsRepository: SettingsRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(settingsRepository) as T
                }
            }
        }
    }
}
