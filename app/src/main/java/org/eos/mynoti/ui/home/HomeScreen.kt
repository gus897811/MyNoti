package org.eos.mynoti.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.eos.mynoti.R
import org.eos.mynoti.data.mock.MockNotificationData
import org.eos.mynoti.di.LocalAppContainer
import org.eos.mynoti.ui.components.EmptyState
import org.eos.mynoti.ui.components.ErrorState
import org.eos.mynoti.ui.components.FilterChip
import org.eos.mynoti.ui.components.LoadingState
import org.eos.mynoti.ui.components.NotificationCard
import org.eos.mynoti.ui.components.SectionHeader
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme
import org.eos.mynoti.ui.util.groupedByDate
import java.time.LocalDateTime

@Composable
fun HomeRoute(
    onNotificationClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            notificationRepository = LocalAppContainer.current.notificationRepository,
            settingsRepository = LocalAppContainer.current.settingsRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onNotificationClick = onNotificationClick,
        onSettingsClick = onSettingsClick,
        onRetry = viewModel::refresh
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onFilterSelected: (HomeFilter) -> Unit,
    onNotificationClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grouped = uiState.visibleNotifications.groupedByDate()

    Column(modifier = modifier.fillMaxSize()) {
        HomeHeader(onSettingsClick = onSettingsClick)
        SectionHeader(
            title = stringResource(R.string.filters),
            modifier = Modifier.padding(
                horizontal = MyNotiDimens.screenHorizontal,
                vertical = MyNotiDimens.spaceSm
            )
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = MyNotiDimens.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing)
        ) {
            items(HomeFilter.entries) { filter ->
                FilterChip(
                    label = filter.label,
                    selected = uiState.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    icon = filter.icon()
                )
            }
        }

        AnimatedContent(
            targetState = Triple(
                uiState.isLoading,
                uiState.errorMessage != null,
                uiState.visibleNotifications.isEmpty()
            ),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "home-list",
            modifier = Modifier.fillMaxSize()
        ) { (isLoading, hasError, isEmpty) ->
            when {
                isLoading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }
                hasError -> {
                    ErrorState(
                        message = uiState.errorMessage.orEmpty(),
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                isEmpty -> {
                    EmptyState(
                        title = if (uiState.selectedFilter == HomeFilter.ALL) {
                            stringResource(R.string.empty_title)
                        } else {
                            stringResource(R.string.empty_filter_title)
                        },
                        description = if (uiState.selectedFilter == HomeFilter.ALL) {
                            stringResource(R.string.empty_description)
                        } else {
                            stringResource(R.string.empty_filter_description)
                        },
                        icon = Icons.Outlined.Notifications
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = MyNotiDimens.screenHorizontal,
                            end = MyNotiDimens.screenHorizontal,
                            top = MyNotiDimens.sectionSpacing,
                            bottom = MyNotiDimens.spaceXxl
                        ),
                        verticalArrangement = Arrangement.spacedBy(MyNotiDimens.itemSpacing)
                    ) {
                        grouped.forEach { (group, items) ->
                            item(key = "header-${group.name}") {
                                SectionHeader(
                                    title = group.label,
                                    modifier = Modifier.padding(
                                        top = MyNotiDimens.spaceSm,
                                        bottom = MyNotiDimens.spaceXs
                                    )
                                )
                            }
                            items(items, key = { it.id }) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onClick = { onNotificationClick(notification.id) },
                                    isImportant = uiState.isImportant(notification)
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
private fun HomeHeader(onSettingsClick: () -> Unit) {
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
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = MyNotiDimens.spaceMd)
        ) {
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

private fun HomeFilter.icon(): ImageVector = when (this) {
    HomeFilter.ALL -> Icons.Outlined.Apps
    HomeFilter.IMPORTANT -> Icons.Outlined.StarOutline
    HomeFilter.KAKAOTALK -> Icons.Outlined.ChatBubbleOutline
    HomeFilter.LEARNING_X -> Icons.Outlined.School
    HomeFilter.HEY_YOUNG -> Icons.Outlined.School
}

@Preview(showBackground = true, name = "Home")
@Preview(showBackground = true, name = "Home compact", widthDp = 320)
@Composable
private fun HomeScreenPreview() {
    MyNotiTheme {
        HomeScreen(
            uiState = HomeUiState(
                notifications = MockNotificationData.create(LocalDateTime.of(2026, 8, 13, 18, 0)),
                isLoading = false
            ),
            onFilterSelected = {},
            onNotificationClick = {},
            onSettingsClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Home empty")
@Composable
private fun HomeScreenEmptyPreview() {
    MyNotiTheme {
        HomeScreen(
            uiState = HomeUiState(isLoading = false),
            onFilterSelected = {},
            onNotificationClick = {},
            onSettingsClick = {},
            onRetry = {}
        )
    }
}
