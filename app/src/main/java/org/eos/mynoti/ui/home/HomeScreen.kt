package org.eos.mynoti.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.eos.mynoti.R
import org.eos.mynoti.data.mock.MockNotificationData
import org.eos.mynoti.di.LocalAppContainer
import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.NotificationFilter
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.domain.model.applyFilter
import org.eos.mynoti.ui.components.EmptyState
import org.eos.mynoti.ui.components.ErrorState
import org.eos.mynoti.ui.components.LoadingState
import org.eos.mynoti.ui.components.MyNotiBrandHeader
import org.eos.mynoti.ui.components.NotificationCard
import org.eos.mynoti.ui.components.SectionHeader
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTheme
import org.eos.mynoti.ui.util.groupedByDate
import java.time.LocalDateTime

@Composable
fun HomeRoute(
    onNotificationClick: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            notificationRepository = LocalAppContainer.current.notificationRepository,
            settingsRepository = LocalAppContainer.current.settingsRepository,
            homeFilterController = LocalAppContainer.current.homeFilterController
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onSelectAllApps = viewModel::selectAllApps,
        onToggleApp = viewModel::toggleApp,
        onRemoveAppFilter = viewModel::removeAppFilter,
        onToggleType = viewModel::toggleType,
        onToggleImportant = viewModel::toggleImportant,
        onQueryChange = viewModel::onSearchQueryChange,
        onClearFilters = viewModel::clearFilters,
        onToggleFiltersExpanded = viewModel::toggleFiltersExpanded,
        onNotificationClick = onNotificationClick,
        onRetry = {}
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSelectAllApps: () -> Unit,
    onToggleApp: (String) -> Unit,
    onRemoveAppFilter: (String) -> Unit,
    onToggleType: (NotificationType) -> Unit,
    onToggleImportant: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onToggleFiltersExpanded: () -> Unit,
    onNotificationClick: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grouped = uiState.visibleNotifications.groupedByDate()
    var showAppFilterSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        MyNotiBrandHeader()
        HomeFilterBar(
            filter = uiState.filter,
            apps = uiState.filterApps,
            resultCount = uiState.visibleNotifications.size,
            expanded = uiState.filtersExpanded,
            onToggleExpanded = onToggleFiltersExpanded,
            onSelectAllApps = onSelectAllApps,
            onRemoveAppFilter = onRemoveAppFilter,
            onAddAppFilterClick = { showAppFilterSheet = true },
            onToggleType = onToggleType,
            onToggleImportant = onToggleImportant,
            onQueryChange = onQueryChange,
            onClear = onClearFilters
        )

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
                    val emptyTitle: String
                    val emptyDescription: String
                    when {
                        uiState.filter.query.isNotBlank() -> {
                            emptyTitle = stringResource(R.string.empty_search_title)
                            emptyDescription = stringResource(R.string.empty_search_description)
                        }
                        uiState.filter.isActive -> {
                            emptyTitle = stringResource(R.string.empty_filter_title)
                            emptyDescription = stringResource(R.string.empty_filter_description)
                        }
                        else -> {
                            emptyTitle = stringResource(R.string.empty_title)
                            emptyDescription = stringResource(R.string.empty_description)
                        }
                    }
                    EmptyState(
                        title = emptyTitle,
                        description = emptyDescription,
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

    if (showAppFilterSheet) {
        HomeAppFilterSheet(
            apps = uiState.filterApps,
            selectedApps = uiState.filter.selectedApps,
            onToggleApp = onToggleApp,
            onDismiss = { showAppFilterSheet = false }
        )
    }
}

@Preview(showBackground = true, name = "Home")
@Preview(showBackground = true, name = "Home compact", widthDp = 320)
@Composable
private fun HomeScreenPreview() {
    val notifications = MockNotificationData.create(LocalDateTime.of(2026, 8, 13, 18, 0))
    MyNotiTheme {
        HomeScreen(
            uiState = HomeUiState(
                visibleNotifications = notifications,
                filtersExpanded = true,
                isLoading = false
            ),
            onSelectAllApps = {},
            onToggleApp = {},
            onRemoveAppFilter = {},
            onToggleType = {},
            onToggleImportant = {},
            onQueryChange = {},
            onClearFilters = {},
            onToggleFiltersExpanded = {},
            onNotificationClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Home filtered")
@Composable
private fun HomeScreenFilteredPreview() {
    val filter = NotificationFilter(
        selectedApps = setOf(org.eos.mynoti.domain.model.AppPackages.LEARNING_X),
        selectedTypes = setOf(NotificationType.ASSIGNMENT),
        importantOnly = true
    )
    val notifications = MockNotificationData.create(LocalDateTime.of(2026, 8, 13, 18, 0))
        .applyFilter(filter)
    MyNotiTheme {
        HomeScreen(
            uiState = HomeUiState(
                visibleNotifications = notifications,
                filter = filter,
                filtersExpanded = true,
                isLoading = false
            ),
            onSelectAllApps = {},
            onToggleApp = {},
            onRemoveAppFilter = {},
            onToggleType = {},
            onToggleImportant = {},
            onQueryChange = {},
            onClearFilters = {},
            onToggleFiltersExpanded = {},
            onNotificationClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Home search")
@Preview(showBackground = true, name = "Home search compact", widthDp = 320)
@Composable
private fun HomeScreenSearchPreview() {
    val filter = NotificationFilter(query = "과제")
    val notifications = MockNotificationData.create(LocalDateTime.of(2026, 8, 13, 18, 0))
        .applyFilter(filter)
    MyNotiTheme {
        HomeScreen(
            uiState = HomeUiState(
                visibleNotifications = notifications,
                filter = filter,
                filtersExpanded = true,
                isLoading = false
            ),
            onSelectAllApps = {},
            onToggleApp = {},
            onRemoveAppFilter = {},
            onToggleType = {},
            onToggleImportant = {},
            onQueryChange = {},
            onClearFilters = {},
            onToggleFiltersExpanded = {},
            onNotificationClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Home search empty")
@Composable
private fun HomeScreenSearchEmptyPreview() {
    val filter = NotificationFilter(query = "없는검색어XYZ")
    MyNotiTheme {
        HomeScreen(
            uiState = HomeUiState(
                visibleNotifications = emptyList(),
                filter = filter,
                filtersExpanded = true,
                isLoading = false
            ),
            onSelectAllApps = {},
            onToggleApp = {},
            onRemoveAppFilter = {},
            onToggleType = {},
            onToggleImportant = {},
            onQueryChange = {},
            onClearFilters = {},
            onToggleFiltersExpanded = {},
            onNotificationClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Home empty")
@Composable
private fun HomeScreenEmptyPreview() {
    MyNotiTheme {
        HomeScreen(
            uiState = HomeUiState(
                settings = AppSettings.defaults(),
                isLoading = false
            ),
            onSelectAllApps = {},
            onToggleApp = {},
            onRemoveAppFilter = {},
            onToggleType = {},
            onToggleImportant = {},
            onQueryChange = {},
            onClearFilters = {},
            onToggleFiltersExpanded = {},
            onNotificationClick = {},
            onRetry = {}
        )
    }
}
