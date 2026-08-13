package org.eos.mynoti.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.eos.mynoti.R
import org.eos.mynoti.domain.model.NotificationAction
import org.eos.mynoti.ui.theme.MyNotiCardShape
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme

@Composable
fun NotificationActionSection(
    actions: List<NotificationAction>,
    onRemindLater: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (actions.isEmpty()) return

    val checked = remember(actions) {
        mutableStateMapOf<Long, Boolean>().apply {
            actions.forEach { put(it.id, it.isCompleted) }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MyNotiCardShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(MyNotiDimens.spaceXl)) {
            Text(
                text = stringResource(R.string.actions_title),
                style = MyNotiTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            actions.forEach { action ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked[action.id] == true,
                        onCheckedChange = { checked[action.id] = it },
                        modifier = Modifier.size(MyNotiDimens.minTouchTarget),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = action.title,
                        style = MyNotiTextStyles.notificationTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))
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

@Preview(showBackground = true)
@Composable
private fun NotificationActionSectionPreview() {
    MyNotiTheme {
        NotificationActionSection(
            actions = listOf(
                NotificationAction(id = 1, title = "운영체제 과제 2 제출")
            ),
            onRemindLater = {},
            modifier = Modifier.padding(MyNotiDimens.screenHorizontal)
        )
    }
}
