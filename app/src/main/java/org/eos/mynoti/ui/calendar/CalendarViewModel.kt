package org.eos.mynoti.ui.calendar

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
import org.eos.mynoti.data.repository.ManualCalendarEventRepository
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.domain.model.CalendarEvent
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.domain.model.applyAppSettings
import org.eos.mynoti.domain.model.toCalendarEvents
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

data class CalendarUiState(
    val currentMonth: YearMonth,
    val selectedDate: LocalDate,
    val today: LocalDate,
    val events: List<CalendarEvent> = emptyList(),
    val isLoading: Boolean = false
) {
    val eventsByDate: Map<LocalDate, List<CalendarEvent>> = events.groupBy { it.date }

    val selectedEvents: List<CalendarEvent>
        get() = eventsByDate[selectedDate].orEmpty().sortedBy { it.eventAt }

    fun typesOn(date: LocalDate): List<NotificationType> {
        return eventsByDate[date].orEmpty()
            .map { it.type }
            .distinct()
            .take(3)
    }
}

class CalendarViewModel(
    notificationRepository: NotificationRepository,
    settingsRepository: SettingsRepository,
    private val manualCalendarEventRepository: ManualCalendarEventRepository,
    today: LocalDate = LocalDate.now()
) : ViewModel() {

    private val currentMonth = MutableStateFlow(YearMonth.from(today))
    private val selectedDate = MutableStateFlow(today)

    val uiState: StateFlow<CalendarUiState> = combine(
        notificationRepository.observeNotifications(),
        settingsRepository.settings,
        manualCalendarEventRepository.observeEvents(),
        currentMonth,
        selectedDate
    ) { notifications, settings, manualEvents, month, date ->
        val fromNotifications = notifications
            .applyAppSettings(settings)
            .toCalendarEvents(settings.highlightKeywords)
        CalendarUiState(
            currentMonth = month,
            selectedDate = date,
            today = today,
            events = (fromNotifications + manualEvents).sortedBy { it.eventAt },
            isLoading = false
        )
    }.catch {
        emit(
            CalendarUiState(
                currentMonth = currentMonth.value,
                selectedDate = selectedDate.value,
                today = today,
                isLoading = false
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(
            currentMonth = YearMonth.from(today),
            selectedDate = today,
            today = today,
            isLoading = true
        )
    )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        currentMonth.value = YearMonth.from(date)
    }

    fun goToPreviousMonth() {
        shiftMonth(-1)
    }

    fun goToNextMonth() {
        shiftMonth(1)
    }

    fun goToToday() {
        val today = uiState.value.today
        if (selectedDate.value == today && currentMonth.value == YearMonth.from(today)) {
            return
        }
        currentMonth.value = YearMonth.from(today)
        selectedDate.value = today
    }

    fun selectMonth(month: YearMonth) {
        if (currentMonth.value == month) return
        currentMonth.value = month
        selectedDate.update { current -> month.shiftedSelection(current) }
    }

    fun addEvent(
        title: String,
        location: String?,
        eventAt: LocalDateTime,
        type: NotificationType,
        isImportant: Boolean
    ) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            manualCalendarEventRepository.add(
                title = trimmed,
                location = location,
                eventAt = eventAt,
                type = type,
                isImportant = isImportant
            )
            selectDate(eventAt.toLocalDate())
        }
    }

    private fun shiftMonth(delta: Long) {
        currentMonth.update { month ->
            val next = month.plusMonths(delta)
            selectedDate.update { current -> next.shiftedSelection(current) }
            next
        }
    }

    companion object {
        fun factory(
            notificationRepository: NotificationRepository,
            settingsRepository: SettingsRepository,
            manualCalendarEventRepository: ManualCalendarEventRepository,
            today: LocalDate = LocalDate.now()
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CalendarViewModel(
                        notificationRepository,
                        settingsRepository,
                        manualCalendarEventRepository,
                        today
                    ) as T
                }
            }
        }
    }
}
