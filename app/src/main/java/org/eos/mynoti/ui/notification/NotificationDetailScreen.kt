package org.eos.mynoti.ui.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.eos.mynoti.R
import org.eos.mynoti.data.mock.MockNotificationData
import org.eos.mynoti.di.LocalAppContainer
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.ui.components.AppIcon
import org.eos.mynoti.ui.components.EmptyState
import org.eos.mynoti.ui.components.ImportanceBadge
import org.eos.mynoti.ui.components.LoadingState
import org.eos.mynoti.ui.components.NotificationActionSection
import org.eos.mynoti.ui.components.TypeBadge
import org.eos.mynoti.ui.theme.MyNotiCardShape
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme
import org.eos.mynoti.ui.util.toReceivedTimestamp
import java.time.LocalDateTime

@Composable
fun NotificationDetailRoute(
    notificationId: Long,
    onBack: () -> Unit,
    viewModel: NotificationDetailViewModel = viewModel(
        key = notificationId.toString(),
        factory = NotificationDetailViewModel.factory(
            notificationId = notificationId,
            repository = LocalAppContainer.current.notificationRepository,
            settingsRepository = LocalAppContainer.current.settingsRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onToggleImportant = viewModel::toggleImportant
    )
}

@Composable
fun NotificationDetailScreen(
    uiState: NotificationDetailUiState,
    onBack: () -> Unit,
    onToggleImportant: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = MyNotiDimens.spaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
            uiState.notification == null -> {
                EmptyState(
                    title = stringResource(R.string.notification_not_found_title),
                    description = stringResource(R.string.notification_not_found_description)
                )
            }
            else -> {
                NotificationDetailContent(
                    notification = uiState.notification,
                    isImportant = uiState.isImportant,
                    onToggleImportant = onToggleImportant,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = MyNotiDimens.screenHorizontal,
                            vertical = MyNotiDimens.screenVertical
                        )
                )
            }
        }
    }
}

@Composable
private fun NotificationDetailContent(
    notification: Notification,
    isImportant: Boolean,
    onToggleImportant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                appPackageName = notification.appPackageName,
                size = MyNotiDimens.appIconLarge,
                contentDescription = notification.appName
            )
            Spacer(modifier = Modifier.width(MyNotiDimens.spaceMd))
            Text(
                text = notification.appName,
                style = MyNotiTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceXl))
        Text(
            text = notification.title,
            style = MyNotiTextStyles.appTitle,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (isImportant) {
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            ImportanceBadge()
        }
        TextButton(onClick = onToggleImportant) {
            Text(
                text = if (notification.isImportant) {
                    stringResource(R.string.unmark_important)
                } else {
                    stringResource(R.string.mark_important)
                }
            )
        }
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceXl))

        DetailSection(
            title = stringResource(R.string.ai_summary),
            emphasized = true
        ) {
            Text(
                text = when {
                    notification.isAnalysisPending -> stringResource(R.string.analysis_pending)
                    !notification.summary.isNullOrBlank() -> notification.summary
                    else -> stringResource(R.string.no_summary)
                },
                style = MyNotiTextStyles.notificationTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))

        DetailSection(title = stringResource(R.string.information)) {
            InfoRow(
                label = stringResource(R.string.label_type),
                trailing = { TypeBadge(type = notification.type) }
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            InfoRow(
                label = stringResource(R.string.label_received),
                value = notification.receivedAt.toReceivedTimestamp()
            )
        }
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))

        DetailSection(title = stringResource(R.string.original_notification)) {
            Text(
                text = notification.content,
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (notification.actions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            NotificationActionSection(
                actions = notification.actions,
                onRemindLater = {}
            )
        }
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceXxl))
    }
}

@Composable
private fun DetailSection(
    title: String,
    emphasized: Boolean = false,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MyNotiCardShape,
        color = if (emphasized) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Column(modifier = Modifier.padding(MyNotiDimens.spaceXl)) {
            Text(
                text = title,
                style = MyNotiTextStyles.metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MyNotiTextStyles.metadata,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (trailing != null) {
            trailing()
        } else if (value != null) {
            Text(
                text = value,
                style = MyNotiTextStyles.notificationTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true, name = "Detail important")
@Preview(showBackground = true, name = "Detail compact", widthDp = 320)
@Composable
private fun NotificationDetailScreenPreview() {
    val sample = MockNotificationData.create(LocalDateTime.of(2026, 8, 13, 10, 30))
        .first { it.isImportant && it.actions.isNotEmpty() }
    MyNotiTheme {
        NotificationDetailScreen(
            uiState = NotificationDetailUiState(
                notification = sample,
                isImportant = true,
                isLoading = false
            ),
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Detail missing")
@Composable
private fun NotificationDetailMissingPreview() {
    MyNotiTheme {
        NotificationDetailScreen(
            uiState = NotificationDetailUiState(notification = null, isLoading = false),
            onBack = {}
        )
    }
}
