package org.eos.mynoti.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.eos.mynoti.BuildConfig
import org.eos.mynoti.R
import org.eos.mynoti.data.filterInstalledApps
import org.eos.mynoti.di.LocalAppContainer
import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.TargetApp
import org.eos.mynoti.ui.components.AppIcon
import org.eos.mynoti.ui.components.KeywordChipGroup
import org.eos.mynoti.ui.components.SectionHeader
import org.eos.mynoti.ui.theme.MyNotiCardShape
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme

private enum class KeywordDialogType { HIGHLIGHT, MUTE }

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(
            settingsRepository = LocalAppContainer.current.settingsRepository,
            installedAppCatalog = LocalAppContainer.current.installedAppCatalog,
            notificationIngest = LocalAppContainer.current.notificationIngest
        )
    )
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val installedAppsLoading by viewModel.installedAppsLoading.collectAsStateWithLifecycle()
    val pickerQuery by viewModel.pickerQuery.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { resId ->
            snackbarHostState.showSnackbar(context.getString(resId))
        }
    }

    LaunchedEffect(showPicker) {
        if (showPicker) {
            viewModel.setPickerQuery("")
            viewModel.loadInstalledApps()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(
            settings = settings,
            onBack = onBack,
            onTargetAppToggled = viewModel::onTargetAppToggled,
            onAddTargetAppClick = { showPicker = true },
            onRemoveTargetApp = viewModel::removeTargetApp,
            onAddHighlightKeyword = viewModel::addHighlightKeyword,
            onRemoveHighlightKeyword = viewModel::removeHighlightKeyword,
            onAddMuteKeyword = viewModel::addMuteKeyword,
            onRemoveMuteKeyword = viewModel::removeMuteKeyword,
            onOpenNotificationAccess = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
            onAddSampleNotification = viewModel::addLearningXSample,
            showDebugActions = BuildConfig.DEBUG
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showPicker) {
        val addedPackages = settings.targetApps.map { it.packageName }.toSet()
        val visibleApps = filterInstalledApps(installedApps, addedPackages, pickerQuery)
        InstalledAppPickerSheet(
            apps = visibleApps,
            query = pickerQuery,
            isLoading = installedAppsLoading,
            hasQuery = pickerQuery.trim().isNotEmpty(),
            onQueryChange = viewModel::setPickerQuery,
            onAppClick = { app -> viewModel.addTargetApp(app.packageName, app.label) },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onTargetAppToggled: (String, Boolean) -> Unit,
    onAddTargetAppClick: () -> Unit,
    onRemoveTargetApp: (String) -> Unit,
    onAddHighlightKeyword: (String) -> Unit,
    onRemoveHighlightKeyword: (String) -> Unit,
    onAddMuteKeyword: (String) -> Unit,
    onRemoveMuteKeyword: (String) -> Unit,
    onOpenNotificationAccess: () -> Unit = {},
    onAddSampleNotification: () -> Unit = {},
    showDebugActions: Boolean = false,
    modifier: Modifier = Modifier
) {
    var dialogType by remember { mutableStateOf<KeywordDialogType?>(null) }

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
            Text(
                text = stringResource(R.string.settings_title),
                style = MyNotiTextStyles.appTitle,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = MyNotiDimens.screenHorizontal,
                    vertical = MyNotiDimens.screenVertical
                )
        ) {
            SectionHeader(title = stringResource(R.string.target_apps))
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
            Text(
                text = stringResource(R.string.target_apps_description),
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MyNotiCardShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(vertical = MyNotiDimens.spaceXs)) {
                    if (settings.targetApps.isEmpty()) {
                        Text(
                            text = stringResource(R.string.target_apps_empty),
                            style = MyNotiTextStyles.notificationSummary,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = MyNotiDimens.cardPadding,
                                vertical = MyNotiDimens.spaceMd
                            )
                        )
                    } else {
                        settings.targetApps.forEach { app ->
                            TargetAppRow(
                                app = app,
                                onToggle = { onTargetAppToggled(app.packageName, it) },
                                onRemove = { onRemoveTargetApp(app.packageName) }
                            )
                        }
                    }
                    AddTargetAppRow(onClick = onAddTargetAppClick)
                }
            }

            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXxl))
            SectionHeader(title = stringResource(R.string.notification_access))
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
            Text(
                text = stringResource(R.string.notification_access_description),
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            OutlinedButton(
                onClick = onOpenNotificationAccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.notification_access_open))
            }

            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXxl))
            SectionHeader(title = stringResource(R.string.highlight_keywords))
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
            Text(
                text = stringResource(R.string.highlight_keywords_description),
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            KeywordChipGroup(
                keywords = settings.highlightKeywords,
                onAddClick = { dialogType = KeywordDialogType.HIGHLIGHT },
                onRemove = onRemoveHighlightKeyword,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXxl))
            SectionHeader(title = stringResource(R.string.mute_keywords))
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
            Text(
                text = stringResource(R.string.mute_keywords_description),
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
            KeywordChipGroup(
                keywords = settings.muteKeywords,
                onAddClick = { dialogType = KeywordDialogType.MUTE },
                onRemove = onRemoveMuteKeyword,
                modifier = Modifier.fillMaxWidth()
            )

            if (showDebugActions) {
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceXxl))
                SectionHeader(title = stringResource(R.string.debug_section))
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
                Text(
                    text = stringResource(R.string.debug_add_sample_description),
                    style = MyNotiTextStyles.notificationSummary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
                Button(
                    onClick = onAddSampleNotification,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.debug_add_sample))
                }
            }
            Spacer(modifier = Modifier.height(MyNotiDimens.spaceXxl))
        }
    }

    dialogType?.let { type ->
        AddKeywordDialog(
            title = if (type == KeywordDialogType.HIGHLIGHT) {
                stringResource(R.string.add_highlight_keyword)
            } else {
                stringResource(R.string.add_mute_keyword)
            },
            onDismiss = { dialogType = null },
            onConfirm = { keyword ->
                when (type) {
                    KeywordDialogType.HIGHLIGHT -> onAddHighlightKeyword(keyword)
                    KeywordDialogType.MUTE -> onAddMuteKeyword(keyword)
                }
                dialogType = null
            }
        )
    }
}

@Composable
private fun TargetAppRow(
    app: TargetApp,
    onToggle: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MyNotiDimens.minTouchTarget)
            .padding(horizontal = MyNotiDimens.cardPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            appPackageName = app.packageName,
            size = MyNotiDimens.appIconSmall,
            contentDescription = app.name
        )
        Text(
            text = app.name,
            style = MyNotiTextStyles.notificationTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MyNotiDimens.spaceMd)
        )
        Switch(
            checked = app.enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.cd_remove_target_app, app.name),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddTargetAppRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MyNotiDimens.minTouchTarget)
            .clickable(onClick = onClick)
            .padding(horizontal = MyNotiDimens.cardPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(MyNotiDimens.appIconSmall)
        )
        Text(
            text = stringResource(R.string.add_target_app),
            style = MyNotiTextStyles.notificationTitle,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = MyNotiDimens.spaceMd)
        )
    }
}

@Composable
private fun AddKeywordDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MyNotiTextStyles.sectionTitle) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.keyword_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                enabled = value.trim().isNotEmpty()
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true, name = "Settings")
@Preview(showBackground = true, name = "Settings compact", widthDp = 320)
@Composable
private fun SettingsScreenPreview() {
    MyNotiTheme {
        SettingsScreen(
            settings = AppSettings.defaults(),
            onBack = {},
            onTargetAppToggled = { _, _ -> },
            onAddTargetAppClick = {},
            onRemoveTargetApp = {},
            onAddHighlightKeyword = {},
            onRemoveHighlightKeyword = {},
            onAddMuteKeyword = {},
            onRemoveMuteKeyword = {},
            showDebugActions = true
        )
    }
}
