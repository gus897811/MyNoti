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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import org.eos.mynoti.R
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
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
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(selectedDate) }
    var time by remember { mutableStateOf(LocalTime.of(15, 0)) }
    var type by remember { mutableStateOf(NotificationType.ETC) }
    var isImportant by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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
                text = stringResource(R.string.calendar_add_event),
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
                        selected = type == item,
                        onClick = { type = item },
                        label = { Text(text = item.label) }
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
                Text(text = stringResource(R.string.add))
            }
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceLg))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
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
            DatePicker(state = datePickerState)
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
                TimePicker(state = timePickerState)
            }
        )
    }
}
