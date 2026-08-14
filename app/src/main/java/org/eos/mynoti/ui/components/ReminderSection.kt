package org.eos.mynoti.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.eos.mynoti.R
import org.eos.mynoti.domain.model.Reminder
import org.eos.mynoti.ui.theme.MyNotiCardShape
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.util.toReceivedTimestamp

@Composable
fun ReminderSection(
    reminders: List<Reminder>,
    showScheduleButton: Boolean,
    onRemindLater: () -> Unit,
    onCancelReminder: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (reminders.isEmpty() && !showScheduleButton) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MyNotiCardShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(MyNotiDimens.spaceXl)) {
            Text(
                text = stringResource(R.string.reminders_title),
                style = MyNotiTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (reminders.isEmpty()) {
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
                Text(
                    text = stringResource(R.string.reminders_empty),
                    style = MyNotiTextStyles.notificationSummary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
                reminders.forEach { reminder ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reminder.remindAt.toReceivedTimestamp(),
                                style = MyNotiTextStyles.notificationTitle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(
                                    if (reminder.isFired) {
                                        R.string.reminder_status_fired
                                    } else {
                                        R.string.reminder_status_scheduled
                                    }
                                ),
                                style = MyNotiTextStyles.caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!reminder.isFired) {
                            IconButton(onClick = { onCancelReminder(reminder.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.cd_cancel_reminder)
                                )
                            }
                        }
                    }
                }
            }
            if (showScheduleButton) {
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
                OutlinedButton(
                    onClick = onRemindLater,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MyNotiCardShape
                ) {
                    Text(text = stringResource(R.string.remind_later))
                }
            }
        }
    }
}
