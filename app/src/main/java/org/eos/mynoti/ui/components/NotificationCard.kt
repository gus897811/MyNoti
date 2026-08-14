package org.eos.mynoti.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.eos.mynoti.R
import org.eos.mynoti.data.mock.MockNotificationData
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.ui.theme.ImportantCardBackground
import org.eos.mynoti.ui.theme.ImportantAccent
import org.eos.mynoti.ui.theme.MyNotiCardShape
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme
import org.eos.mynoti.ui.util.previewText
import org.eos.mynoti.ui.util.toReceivedTimeLabel
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isImportant: Boolean = notification.isImportant
) {
    val isToday = notification.receivedAt.toLocalDate() == LocalDate.now()
    val hasAiSummary = !notification.summary.isNullOrBlank()
    val containerColor = if (isImportant) {
        ImportantCardBackground
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MyNotiDimens.minTouchTarget)
            .semantics { role = Role.Button },
        shape = MyNotiCardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            if (isImportant) {
                Box(
                    modifier = Modifier
                        .width(MyNotiDimens.importantAccent)
                        .fillMaxHeight()
                        .background(ImportantAccent)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MyNotiDimens.cardPadding),
                verticalAlignment = Alignment.Top
            ) {
                AppIcon(
                    appPackageName = notification.appPackageName,
                    contentDescription = notification.appName
                )
                Spacer(modifier = Modifier.width(MyNotiDimens.spaceMd))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.appName,
                            style = MyNotiTextStyles.metadata,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = notification.receivedAt.toReceivedTimeLabel(),
                            style = MyNotiTextStyles.caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isToday) {
                            Spacer(modifier = Modifier.width(MyNotiDimens.spaceSm))
                            Box(
                                modifier = Modifier
                                    .size(MyNotiDimens.unreadDot)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
                    Text(
                        text = notification.title,
                        style = MyNotiTextStyles.notificationTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
                    Text(
                        text = if (notification.isAnalysisPending) {
                            stringResource(R.string.analysis_pending)
                        } else {
                            notification.previewText()
                        },
                        style = MyNotiTextStyles.notificationSummary.copy(
                            fontWeight = if (hasAiSummary) FontWeight.Medium else FontWeight.Normal
                        ),
                        color = if (hasAiSummary) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.spaceSm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isImportant) {
                            ImportanceBadge()
                        }
                        TypeBadge(type = notification.type)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Important card")
@Composable
private fun NotificationCardImportantPreview() {
    MyNotiTheme {
        NotificationCard(
            notification = MockNotificationData.create(
                LocalDateTime.of(2026, 8, 13, 10, 30)
            ).first { it.isImportant },
            onClick = {},
            modifier = Modifier.padding(MyNotiDimens.screenHorizontal)
        )
    }
}

@Preview(showBackground = true, name = "Regular card")
@Composable
private fun NotificationCardRegularPreview() {
    MyNotiTheme {
        NotificationCard(
            notification = MockNotificationData.create(
                LocalDateTime.of(2026, 8, 13, 10, 30)
            ).first { !it.isImportant },
            onClick = {},
            modifier = Modifier.padding(MyNotiDimens.screenHorizontal)
        )
    }
}
