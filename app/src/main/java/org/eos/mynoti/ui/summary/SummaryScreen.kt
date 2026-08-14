package org.eos.mynoti.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.eos.mynoti.R
import org.eos.mynoti.di.LocalAppContainer
import org.eos.mynoti.domain.model.DailySummary
import org.eos.mynoti.domain.model.ReminderItem
import org.eos.mynoti.domain.model.ReminderTimeGroup
import org.eos.mynoti.domain.model.SummaryTask
import org.eos.mynoti.ui.components.EmptyState
import org.eos.mynoti.ui.components.ErrorState
import org.eos.mynoti.ui.components.LoadingState
import org.eos.mynoti.ui.components.SectionHeader
import org.eos.mynoti.ui.theme.ImportantAccent
import org.eos.mynoti.ui.theme.MyNotiCardShape
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme
import org.eos.mynoti.ui.theme.TypeAssignment
import org.eos.mynoti.ui.util.toReminderGroupLabel
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun SummaryRoute(
    onNotificationClick: (Long) -> Unit,
    viewModel: SummaryViewModel = viewModel(
        factory = SummaryViewModel.factory(
            summaryRepository = LocalAppContainer.current.summaryRepository,
            reminderRepository = LocalAppContainer.current.reminderRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SummaryScreen(
        uiState = uiState,
        onNotificationClick = onNotificationClick,
        onRetry = {}
    )
}

@Composable
fun SummaryScreen(
    uiState: SummaryUiState,
    onNotificationClick: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(
                horizontal = MyNotiDimens.screenHorizontal,
                vertical = MyNotiDimens.screenVertical
            )
        ) {
            Text(
                text = greetingText(),
                style = MyNotiTextStyles.appTitle,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
            Text(
                text = stringResource(R.string.today_summary),
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize()
                )
            }
            uiState.summary == null -> {
                EmptyState(
                    title = stringResource(R.string.empty_summary_title),
                    description = stringResource(R.string.empty_summary_description),
                    icon = Icons.Outlined.AutoAwesome
                )
            }
            else -> {
                val summary = uiState.summary
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = MyNotiDimens.screenHorizontal,
                        end = MyNotiDimens.screenHorizontal,
                        top = MyNotiDimens.spaceSm,
                        bottom = MyNotiDimens.spaceXxl
                    ),
                    verticalArrangement = Arrangement.spacedBy(MyNotiDimens.sectionSpacing)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.spaceSm)
                        ) {
                            SummaryStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Whatshot,
                                iconTint = ImportantAccent,
                                label = stringResource(R.string.stat_important),
                                value = summary.importantCount.toString()
                            )
                            SummaryStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.AutoMirrored.Outlined.Assignment,
                                iconTint = TypeAssignment,
                                label = stringResource(R.string.stat_assignment),
                                value = summary.assignmentCount.toString()
                            )
                            SummaryStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Event,
                                iconTint = MaterialTheme.colorScheme.primary,
                                label = stringResource(R.string.stat_upcoming),
                                value = summary.upcomingEventCount.toString()
                            )
                        }
                    }
                    item {
                        SectionHeader(
                            title = stringResource(R.string.most_important_today),
                            modifier = Modifier.padding(top = MyNotiDimens.spaceSm)
                        )
                    }
                    if (summary.urgentItems.isEmpty()) {
                        item {
                            EmptyState(
                                title = stringResource(R.string.empty_urgent_title),
                                description = stringResource(R.string.empty_urgent_description)
                            )
                        }
                    } else {
                        items(summary.urgentItems, key = { it.notificationId }) { task ->
                            UrgentTaskCard(
                                task = task,
                                onClick = { onNotificationClick(task.notificationId) }
                            )
                        }
                    }
                    item {
                        AiInsightCard(insight = summary.insight)
                    }
                    item {
                        SectionHeader(
                            title = stringResource(R.string.summary_reminders),
                            modifier = Modifier.padding(top = MyNotiDimens.spaceSm)
                        )
                    }
                    if (uiState.reminderGroups.isEmpty()) {
                        item {
                            EmptyState(
                                title = stringResource(R.string.empty_reminders_title),
                                description = stringResource(R.string.empty_reminders_description)
                            )
                        }
                    } else {
                        uiState.reminderGroups.forEach { group ->
                            item(key = "reminder-group-${group.remindAt}") {
                                ReminderTimeGroupCard(
                                    group = group,
                                    onNotificationClick = onNotificationClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MyNotiCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(MyNotiDimens.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(MyNotiDimens.spaceXl)
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))
            Text(
                text = value,
                style = MyNotiTextStyles.appTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MyNotiTextStyles.caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UrgentTaskCard(
    task: SummaryTask,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MyNotiCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(MyNotiDimens.cardPadding)) {
            Text(
                text = task.title,
                style = MyNotiTextStyles.notificationTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
            Text(
                text = task.dueLabel,
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun AiInsightCard(insight: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MyNotiCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(MyNotiDimens.spaceXl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(MyNotiDimens.spaceXl)
                )
                Text(
                    text = stringResource(R.string.ai_insight),
                    style = MyNotiTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = MyNotiDimens.spaceSm)
                )
            }
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))
            Text(
                text = insight ?: stringResource(R.string.ai_insight_placeholder),
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ReminderTimeGroupCard(
    group: ReminderTimeGroup,
    onNotificationClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MyNotiCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(MyNotiDimens.cardPadding)) {
            Text(
                text = group.remindAt.toReminderGroupLabel(),
                style = MyNotiTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            group.items.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))
                }
                ReminderItemRow(
                    item = item,
                    onClick = { onNotificationClick(item.notificationId) }
                )
            }
        }
    }
}

@Composable
private fun ReminderItemRow(
    item: ReminderItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MyNotiCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(MyNotiDimens.spaceMd)) {
            Text(
                text = item.title,
                style = MyNotiTextStyles.notificationTitle,
                color = if (item.isFired) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (item.isFired) TextDecoration.LineThrough else null
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
            Text(
                text = item.appName,
                style = MyNotiTextStyles.caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = if (item.isFired) TextDecoration.LineThrough else null
            )
        }
    }
}

@Composable
private fun greetingText(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> stringResource(R.string.greeting_morning)
        hour < 18 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
}

@Preview(showBackground = true, name = "Summary")
@Preview(showBackground = true, name = "Summary compact", widthDp = 320)
@Composable
private fun SummaryScreenPreview() {
    MyNotiTheme {
        SummaryScreen(
            uiState = SummaryUiState(
                summary = DailySummary(
                    importantCount = 3,
                    assignmentCount = 2,
                    upcomingEventCount = 1,
                    mostUrgentTask = "운영체제 과제 2 · 내일 23:59 마감",
                    insight = "오늘은 운영체제 과제의 마감이 가장 가까우므로 먼저 처리하는 것을 추천합니다.",
                    urgentItems = listOf(
                        SummaryTask("운영체제 과제 2", "내일 23:59 마감", 1),
                        SummaryTask("국가장학금 신청", "9월 10일 마감", 4)
                    )
                ),
                reminderGroups = listOf(
                    ReminderTimeGroup(
                        remindAt = LocalDateTime.of(2026, 8, 14, 20, 0),
                        items = listOf(
                            ReminderItem(
                                id = 1,
                                notificationId = 1,
                                title = "운영체제 과제 2 제출",
                                appName = "LearningX",
                                remindAt = LocalDateTime.of(2026, 8, 14, 20, 0),
                                isFired = true
                            )
                        )
                    ),
                    ReminderTimeGroup(
                        remindAt = LocalDateTime.of(2026, 8, 15, 12, 0),
                        items = listOf(
                            ReminderItem(
                                id = 2,
                                notificationId = 4,
                                title = "국가장학금 2차 신청",
                                appName = "헤이영캠퍼스",
                                remindAt = LocalDateTime.of(2026, 8, 15, 12, 0),
                                isFired = false
                            )
                        )
                    )
                ),
                isLoading = false
            ),
            onNotificationClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Summary loading")
@Composable
private fun SummaryLoadingPreview() {
    MyNotiTheme {
        SummaryScreen(
            uiState = SummaryUiState(isLoading = true),
            onNotificationClick = {},
            onRetry = {}
        )
    }
}
