package org.eos.mynoti.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.eos.mynoti.R
import org.eos.mynoti.di.LocalAppContainer
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.CalendarEvent
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.ui.components.EmptyState
import org.eos.mynoti.ui.components.MyNotiBrandHeader
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@Composable
fun CalendarRoute(
    onNotificationClick: (Long) -> Unit,
    viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModel.factory(
            notificationRepository = LocalAppContainer.current.notificationRepository,
            settingsRepository = LocalAppContainer.current.settingsRepository,
            manualCalendarEventRepository = LocalAppContainer.current.manualCalendarEventRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarScreen(
        uiState = uiState,
        onSelectDate = viewModel::selectDate,
        onPreviousMonth = viewModel::goToPreviousMonth,
        onNextMonth = viewModel::goToNextMonth,
        onSelectMonth = viewModel::selectMonth,
        onToday = viewModel::goToToday,
        onEventClick = { event ->
            event.notificationId?.let(onNotificationClick)
        },
        onAddEvent = viewModel::addEvent
    )
}

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onToday: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEvent: (String, String?, LocalDateTime, NotificationType, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun expandCalendar() {
        scope.launch { listState.animateScrollToItem(0) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        CalendarScreenHeader(onAddEvent = { showAddSheet = true })
        CalendarMonthHeader(
            month = uiState.currentMonth,
            onPrevious = {
                onPreviousMonth()
                expandCalendar()
            },
            onNext = {
                onNextMonth()
                expandCalendar()
            },
            onMonthClick = { showMonthPicker = true },
            onToday = {
                onToday()
                expandCalendar()
            },
            modifier = Modifier.padding(horizontal = MyNotiDimens.spaceSm)
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = MyNotiDimens.spaceXxl)
        ) {
            item(key = "calendar-grid") {
                Column(
                    modifier = Modifier.padding(horizontal = MyNotiDimens.screenHorizontal)
                ) {
                    CalendarWeekdayHeader(
                        modifier = Modifier.padding(vertical = MyNotiDimens.spaceSm)
                    )
                    CalendarMonthGrid(
                        month = uiState.currentMonth,
                        selectedDate = uiState.selectedDate,
                        today = uiState.today,
                        markersByDate = uiState::markersOn,
                        onSelectDate = onSelectDate
                    )
                }
            }
            item(key = "selected-date") {
                CalendarSelectedDateTitle(
                    date = uiState.selectedDate,
                    modifier = Modifier.padding(
                        horizontal = MyNotiDimens.screenHorizontal,
                        vertical = MyNotiDimens.spaceSm
                    )
                )
            }
            if (uiState.selectedEvents.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        title = stringResource(R.string.calendar_empty_title),
                        description = stringResource(R.string.calendar_empty_description),
                        icon = Icons.Outlined.EventBusy
                    )
                }
            } else {
                items(
                    items = uiState.selectedEvents,
                    key = { it.listKey }
                ) { event ->
                    CalendarEventCard(
                        event = event,
                        onClick = { onEventClick(event) },
                        modifier = Modifier.padding(
                            horizontal = MyNotiDimens.screenHorizontal,
                            vertical = MyNotiDimens.itemSpacing / 2
                        )
                    )
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = uiState.currentMonth,
            onConfirm = { month ->
                onSelectMonth(month)
                showMonthPicker = false
                expandCalendar()
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    if (showAddSheet) {
        AddCalendarEventSheet(
            selectedDate = uiState.selectedDate,
            onDismiss = { showAddSheet = false },
            onSave = { title, location, eventAt, type, isImportant ->
                onAddEvent(title, location, eventAt, type, isImportant)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun CalendarScreenHeader(onAddEvent: () -> Unit) {
    MyNotiBrandHeader {
        CalendarAddEventButton(onClick = onAddEvent)
    }
}

@Preview(showBackground = true, name = "Calendar")
@Preview(showBackground = true, name = "Calendar compact", widthDp = 320)
@Composable
private fun CalendarScreenPreview() {
    val selected = LocalDate.of(2026, 8, 10)
    MyNotiTheme {
        CalendarScreen(
            uiState = CalendarUiState(
                currentMonth = YearMonth.of(2026, 8),
                selectedDate = selected,
                today = selected,
                events = previewCalendarEvents(),
                isLoading = false
            ),
            onSelectDate = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onSelectMonth = {},
            onToday = {},
            onEventClick = {},
            onAddEvent = { _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Calendar empty")
@Composable
private fun CalendarScreenEmptyPreview() {
    val selected = LocalDate.of(2026, 8, 12)
    MyNotiTheme {
        CalendarScreen(
            uiState = CalendarUiState(
                currentMonth = YearMonth.of(2026, 8),
                selectedDate = selected,
                today = LocalDate.of(2026, 8, 10),
                events = previewCalendarEvents(),
                isLoading = false
            ),
            onSelectDate = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onSelectMonth = {},
            onToday = {},
            onEventClick = {},
            onAddEvent = { _, _, _, _, _ -> }
        )
    }
}

private fun previewCalendarEvents(): List<CalendarEvent> {
    return listOf(
        CalendarEvent(
            notificationId = 6,
            title = "캡스톤 팀플 회의",
            location = "중앙도서관",
            eventAt = LocalDateTime.of(2026, 8, 10, 15, 0),
            receivedAt = LocalDateTime.of(2026, 8, 10, 8, 40),
            appName = "카카오톡",
            appPackageName = AppPackages.KAKAOTALK,
            type = NotificationType.COMMUNICATION,
            isImportant = true
        ),
        CalendarEvent(
            notificationId = 7,
            title = "수아 점심 약속",
            location = "학생회관",
            eventAt = LocalDateTime.of(2026, 8, 10, 12, 30),
            receivedAt = LocalDateTime.of(2026, 8, 10, 9, 10),
            appName = "카카오톡",
            appPackageName = AppPackages.KAKAOTALK,
            type = NotificationType.COMMUNICATION,
            isImportant = false
        ),
        CalendarEvent(
            notificationId = 1,
            title = "운영체제 과제 제출",
            location = null,
            eventAt = LocalDateTime.of(2026, 8, 10, 23, 59),
            receivedAt = LocalDateTime.of(2026, 8, 10, 10, 5),
            appName = "LearningX Student",
            appPackageName = AppPackages.LEARNING_X,
            type = NotificationType.ASSIGNMENT,
            isImportant = true
        )
    )
}
