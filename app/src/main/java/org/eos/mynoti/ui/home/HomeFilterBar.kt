package org.eos.mynoti.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import org.eos.mynoti.R
import org.eos.mynoti.domain.model.NotificationFilter
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.domain.model.TargetApp
import org.eos.mynoti.ui.components.AppFilterChip
import org.eos.mynoti.ui.components.FilterChipRow
import org.eos.mynoti.ui.components.FilterSection
import org.eos.mynoti.ui.components.icon
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles

@Composable
fun HomeFilterBar(
    filter: NotificationFilter,
    apps: List<TargetApp>,
    resultCount: Int,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectAllApps: () -> Unit,
    onRemoveAppFilter: (String) -> Unit,
    onAddAppFilterClick: () -> Unit,
    onToggleType: (NotificationType) -> Unit,
    onToggleImportant: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MyNotiDimens.screenHorizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = MyNotiDimens.minTouchTarget)
                    .semantics { role = Role.Button }
                    .clickable(onClick = onToggleExpanded),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filters),
                    style = MyNotiTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = if (expanded) {
                        Icons.Outlined.ExpandLess
                    } else {
                        Icons.Outlined.ExpandMore
                    },
                    contentDescription = stringResource(R.string.cd_toggle_filters),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(start = MyNotiDimens.spaceXs)
                        .size(MyNotiDimens.spaceXl)
                )
            }
            Text(
                text = stringResource(R.string.filter_result_count, resultCount),
                style = MyNotiTextStyles.metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = MyNotiDimens.spaceSm)
            )
            if (filter.isActive) {
                TextButton(onClick = onClear) {
                    Text(text = stringResource(R.string.clear_filters))
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = filter.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MyNotiDimens.screenHorizontal,
                            end = MyNotiDimens.screenHorizontal,
                            top = MyNotiDimens.spaceSm
                        ),
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.filter_search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = if (filter.query.isNotBlank()) {
                        {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.cd_clear_search)
                                )
                            }
                        }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    )
                )
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceSm))

                FilterSection(title = stringResource(R.string.filter_status)) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = MyNotiDimens.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing)
                    ) {
                        item {
                            AppFilterChip(
                                label = stringResource(R.string.filter_important),
                                selected = filter.importantOnly,
                                onClick = onToggleImportant,
                                icon = Icons.Outlined.StarOutline
                            )
                        }
                    }
                }

                FilterSection(title = stringResource(R.string.filter_apps)) {
                    val selectedApps = apps.filter { it.packageName in filter.selectedApps }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onAddAppFilterClick,
                            modifier = Modifier
                                .padding(start = MyNotiDimens.screenHorizontal)
                                .defaultMinSize(
                                    minWidth = MyNotiDimens.filterMinHeight,
                                    minHeight = MyNotiDimens.filterMinHeight
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = stringResource(R.string.cd_add_app_filter),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                start = MyNotiDimens.chipSpacing,
                                end = MyNotiDimens.screenHorizontal
                            ),
                            horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item(key = "all-apps") {
                                AppFilterChip(
                                    label = stringResource(R.string.filter_all_apps),
                                    selected = filter.isAllApps,
                                    onClick = onSelectAllApps,
                                    icon = Icons.Outlined.Apps
                                )
                            }
                            items(selectedApps, key = { it.packageName }) { app ->
                                AppFilterChip(
                                    label = app.name,
                                    selected = true,
                                    onClick = {},
                                    onRemove = { onRemoveAppFilter(app.packageName) },
                                    removeContentDescription = stringResource(
                                        R.string.cd_remove_app_filter,
                                        app.name
                                    )
                                )
                            }
                        }
                    }
                }

                FilterSection(title = stringResource(R.string.filter_types)) {
                    FilterChipRow(
                        items = NotificationType.entries.toList(),
                        label = { it.label },
                        selected = { it in filter.selectedTypes },
                        onClick = onToggleType,
                        icon = { it.icon() },
                        key = { it.name }
                    )
                }
            }
        }
    }
}
