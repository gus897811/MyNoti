package org.eos.mynoti.ui.calendar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import org.eos.mynoti.R
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.CalendarEvent
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.ui.components.FilterChip
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme
import org.eos.mynoti.ui.util.toReceivedTimeLabel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCalendarEventSheet(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        location: String?,
        eventAt: LocalDateTime,
        type: NotificationType,
        isImportant: Boolean
    ) -> Unit,
    editingEvent: CalendarEvent? = null,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = editingEvent != null
    val editKey = editingEvent?.manualEventId
    var title by remember(editKey) { mutableStateOf(editingEvent?.title.orEmpty()) }
    var location by remember(editKey) { mutableStateOf(editingEvent?.location.orEmpty()) }
    var date by remember(editKey) {
        mutableStateOf(editingEvent?.eventAt?.toLocalDate() ?: selectedDate)
    }
    var time by remember(editKey) {
        mutableStateOf(editingEvent?.eventAt?.toLocalTime() ?: LocalTime.of(15, 0))
    }
    var type by remember(editKey) { mutableStateOf(editingEvent?.type ?: NotificationType.ETC) }
    var isImportant by remember(editKey) { mutableStateOf(editingEvent?.isImportant ?: false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = MyNotiDimens.screenHorizontal,
                    vertical = MyNotiDimens.spaceMd
                )
        ) {
            Text(
                text = stringResource(
                    if (isEditing) R.string.calendar_edit_event else R.string.calendar_add_event
                ),
                style = MyNotiTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceLg))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.calendar_event_title)) },
                placeholder = { Text(text = stringResource(R.string.calendar_event_title_placeholder)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.calendar_event_location)) },
                placeholder = { Text(text = stringResource(R.string.calendar_event_location_placeholder)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.spaceSm)
            ) {
                TextButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = stringResource(R.string.calendar_event_date),
                            style = MyNotiTextStyles.caption
                        )
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            style = MyNotiTextStyles.notificationTitle
                        )
                    }
                }
                TextButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = stringResource(R.string.calendar_event_time),
                            style = MyNotiTextStyles.caption
                        )
                        Text(
                            text = LocalDateTime.of(date, time).toReceivedTimeLabel(),
                            style = MyNotiTextStyles.notificationTitle
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            Text(
                text = stringResource(R.string.calendar_event_category),
                style = MyNotiTextStyles.caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing)
            ) {
                NotificationType.entries.forEach { item ->
                    FilterChip(
                        label = item.label,
                        selected = type == item,
                        onClick = { type = item }
                    )
                }
            }
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.calendar_event_important),
                    style = MyNotiTextStyles.notificationTitle,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = isImportant, onCheckedChange = { isImportant = it })
            }
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXl))
            Button(
                onClick = {
                    onSave(
                        title,
                        location.ifBlank { null },
                        LocalDateTime.of(date, time),
                        type,
                        isImportant
                    )
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(
                        if (isEditing) R.string.reminder_save else R.string.add
                    )
                )
            }
            if (onDelete != null) {
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceLg))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        val datePickerColors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
            selectedYearContainerColor = MaterialTheme.colorScheme.primary,
            selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
            todayDateBorderColor = MaterialTheme.colorScheme.primary,
            todayContentColor = MaterialTheme.colorScheme.primary,
            currentYearContentColor = MaterialTheme.colorScheme.primary
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = datePickerColors,
            tonalElevation = 0.dp,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(text = stringResource(R.string.reminder_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState, colors = datePickerColors)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            confirmButton = {
                TextButton(
                    onClick = {
                        time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text(text = stringResource(R.string.reminder_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        clockDialColor = MaterialTheme.colorScheme.surface,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        )
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = stringResource(R.string.calendar_delete_event_title)) },
            text = { Text(text = stringResource(R.string.calendar_delete_event_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Edit calendar event sheet")
@Composable
private fun AddCalendarEventSheetEditPreview() {
    MyNotiTheme {
        AddCalendarEventSheet(
            selectedDate = LocalDate.of(2026, 8, 10),
            editingEvent = CalendarEvent(
                manualEventId = 1,
                title = "캡스톤 팀플 회의",
                location = "중앙도서관",
                eventAt = LocalDateTime.of(2026, 8, 10, 15, 0),
                appName = "직접 추가",
                appPackageName = AppPackages.MANUAL,
                type = NotificationType.COMMUNICATION,
                isImportant = true
            ),
            onDismiss = {},
            onSave = { _, _, _, _, _ -> },
            onDelete = {}
        )
    }
}
