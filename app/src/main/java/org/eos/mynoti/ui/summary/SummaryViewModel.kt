package org.eos.mynoti.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.eos.mynoti.data.repository.SummaryRepository
import org.eos.mynoti.domain.model.DailySummary

data class SummaryUiState(
    val summary: DailySummary? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class SummaryViewModel(
    summaryRepository: SummaryRepository
) : ViewModel() {

    val uiState: StateFlow<SummaryUiState> = summaryRepository.observeDailySummary()
        .map { summary ->
            SummaryUiState(summary = summary, isLoading = false)
        }
        .catch { error ->
            emit(
                SummaryUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "요약을 불러오지 못했습니다."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SummaryUiState()
        )

    companion object {
        fun factory(summaryRepository: SummaryRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SummaryViewModel(summaryRepository) as T
                }
            }
        }
    }
}
