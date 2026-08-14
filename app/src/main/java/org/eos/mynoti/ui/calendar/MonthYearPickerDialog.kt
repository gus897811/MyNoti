package org.eos.mynoti.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.eos.mynoti.R
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import java.time.YearMonth

@Composable
fun MonthYearPickerDialog(
    currentMonth: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember { mutableIntStateOf(currentMonth.year) }
    var month by remember { mutableIntStateOf(currentMonth.monthValue) }

    LaunchedEffect(currentMonth) {
        year = currentMonth.year
        month = currentMonth.monthValue
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = { Text(text = stringResource(R.string.calendar_month_picker_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { year -= 1 }) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronLeft,
                            contentDescription = stringResource(R.string.calendar_previous_year)
                        )
                    }
                    Text(
                        text = "${year}년",
                        style = MyNotiTextStyles.sectionTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = MyNotiDimens.spaceMd)
                    )
                    IconButton(onClick = { year += 1 }) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = stringResource(R.string.calendar_next_year)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
                Column(verticalArrangement = Arrangement.spacedBy(MyNotiDimens.spaceSm)) {
                    (0 until 3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.spaceSm)
                        ) {
                            (1..4).forEach { column ->
                                val value = row * 4 + column
                                MonthPickerCell(
                                    month = value,
                                    selected = month == value,
                                    onClick = { month = value },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth.of(year, month)) }) {
                Text(text = stringResource(R.string.calendar_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun MonthPickerCell(
    month: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = "${month}월"
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                this.selected = selected
            },
        shape = RoundedCornerShape(MyNotiDimens.iconRadius),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    ) {
        Text(
            text = label,
            style = MyNotiTextStyles.notificationTitle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MyNotiDimens.spaceMd),
            textAlign = TextAlign.Center
        )
    }
}
