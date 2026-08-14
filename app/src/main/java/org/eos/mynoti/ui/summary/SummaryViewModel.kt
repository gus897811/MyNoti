package org.eos.mynoti.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.eos.mynoti.data.repository.ReminderRepository
import org.eos.mynoti.data.repository.SummaryRepository
import org.eos.mynoti.domain.model.DailySummary
import org.eos.mynoti.domain.model.ReminderGroupFactory
import org.eos.mynoti.domain.model.ReminderTimeGroup

data class SummaryUiState(
    val summary: DailySummary? = null,
    val reminderGroups: List<ReminderTimeGroup> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class SummaryViewModel(
    summaryRepository: SummaryRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    val uiState: StateFlow<SummaryUiState> = combine(
        summaryRepository.observeDailySummary(),
        reminderRepository.observeVisibleItems()
    ) { summary, reminders ->
        SummaryUiState(
            summary = summary,
            reminderGroups = ReminderGroupFactory.group(reminders),
            isLoading = false
        )
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

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            reminderRepository.cancel(id)
        }
    }

    companion object {
        fun factory(
            summaryRepository: SummaryRepository,
            reminderRepository: ReminderRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SummaryViewModel(summaryRepository, reminderRepository) as T
                }
            }
        }
    }
}
