package org.eos.mynoti.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.data.repository.SummaryRepository
import org.eos.mynoti.domain.model.DailySummary

data class SummaryUiState(
    val summary: DailySummary? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class SummaryViewModel(
    private val summaryRepository: SummaryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collectLatest { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { summaryRepository.getDailySummary() }
                .onSuccess { summary ->
                    _uiState.value = SummaryUiState(summary = summary, isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = SummaryUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "요약을 불러오지 못했습니다."
                    )
                }
        }
    }

    companion object {
        fun factory(
            summaryRepository: SummaryRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SummaryViewModel(summaryRepository, settingsRepository) as T
                }
            }
        }
    }
}
