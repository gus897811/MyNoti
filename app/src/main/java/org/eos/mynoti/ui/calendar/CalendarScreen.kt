package org.eos.mynoti.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.eos.mynoti.R
import org.eos.mynoti.di.LocalAppContainer
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.CalendarEvent
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.ui.components.EmptyState
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
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
    onToday: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEvent: (String, String?, LocalDateTime, NotificationType, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CalendarScreenHeader()
            CalendarMonthHeader(
                month = uiState.currentMonth,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                onToday = onToday,
                modifier = Modifier.padding(horizontal = MyNotiDimens.spaceSm)
            )
            CalendarWeekdayHeader(
                modifier = Modifier.padding(
                    horizontal = MyNotiDimens.screenHorizontal,
                    vertical = MyNotiDimens.spaceSm
                )
            )
            CalendarMonthGrid(
                month = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                today = uiState.today,
                typesByDate = uiState::typesOn,
                onSelectDate = onSelectDate,
                modifier = Modifier.padding(horizontal = MyNotiDimens.screenHorizontal)
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            CalendarSelectedDateTitle(
                date = uiState.selectedDate,
                modifier = Modifier.padding(horizontal = MyNotiDimens.screenHorizontal)
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))
            if (uiState.selectedEvents.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.calendar_empty_title),
                    description = stringResource(R.string.calendar_empty_description),
                    icon = Icons.Outlined.EventBusy,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = MyNotiDimens.screenHorizontal,
                        end = MyNotiDimens.screenHorizontal,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(MyNotiDimens.itemSpacing)
                ) {
                    items(uiState.selectedEvents, key = { it.listKey }) { event ->
                        CalendarEventCard(
                            event = event,
                            onClick = { onEventClick(event) }
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = MyNotiDimens.screenHorizontal,
                    bottom = MyNotiDimens.spaceXl
                ),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.calendar_add_event)
            )
        }
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
private fun CalendarScreenHeader() {
    Column(
        modifier = Modifier.padding(
            start = MyNotiDimens.spaceSm,
            end = MyNotiDimens.spaceSm,
            top = MyNotiDimens.screenVertical,
            bottom = MyNotiDimens.spaceSm
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.height(MyNotiDimens.minTouchTarget),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = stringResource(R.string.cd_menu),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = MyNotiDimens.spaceMd)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        Column(modifier = Modifier.padding(horizontal = MyNotiDimens.spaceMd)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MyNotiTextStyles.appTitle,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
