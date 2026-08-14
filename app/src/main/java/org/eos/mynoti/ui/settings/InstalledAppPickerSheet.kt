package org.eos.mynoti.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.eos.mynoti.R
import org.eos.mynoti.data.InstalledAppInfo
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.ui.components.AppIcon
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledAppPickerSheet(
    apps: List<InstalledAppInfo>,
    query: String,
    isLoading: Boolean,
    hasQuery: Boolean,
    onQueryChange: (String) -> Unit,
    onAppClick: (InstalledAppInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        InstalledAppPickerContent(
            apps = apps,
            query = query,
            isLoading = isLoading,
            hasQuery = hasQuery,
            onQueryChange = onQueryChange,
            onAppClick = onAppClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = MyNotiDimens.screenHorizontal)
        )
    }
}

@Composable
fun InstalledAppPickerContent(
    apps: List<InstalledAppInfo>,
    query: String,
    isLoading: Boolean,
    hasQuery: Boolean,
    onQueryChange: (String) -> Unit,
    onAppClick: (InstalledAppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.target_apps_picker_title),
            style = MyNotiTextStyles.sectionTitle,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.target_apps_search)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .padding(MyNotiDimens.spaceXxl),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            apps.isEmpty() -> {
                Text(
                    text = stringResource(
                        if (hasQuery) {
                            R.string.target_apps_no_search_results
                        } else {
                            R.string.target_apps_none_to_add
                        }
                    ),
                    style = MyNotiTextStyles.notificationSummary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = MyNotiDimens.spaceXl)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    contentPadding = PaddingValues(bottom = MyNotiDimens.spaceLg)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        InstalledAppPickerRow(
                            app = app,
                            onClick = { onAppClick(app) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledAppPickerRow(
    app: InstalledAppInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MyNotiDimens.minTouchTarget)
            .clickable(onClick = onClick)
            .padding(vertical = MyNotiDimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            appPackageName = app.packageName,
            size = MyNotiDimens.appIconSmall,
            contentDescription = app.label
        )
        Spacer(modifier = Modifier.width(MyNotiDimens.spaceMd))
        Text(
            text = app.label,
            style = MyNotiTextStyles.notificationTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, name = "Installed app picker")
@Composable
private fun InstalledAppPickerPreview() {
    MyNotiTheme {
        InstalledAppPickerContent(
            apps = listOf(
                InstalledAppInfo(AppPackages.KAKAOTALK, "KakaoTalk"),
                InstalledAppInfo("com.android.chrome", "Chrome"),
                InstalledAppInfo("com.google.android.gm", "Gmail")
            ),
            query = "",
            isLoading = false,
            hasQuery = false,
            onQueryChange = {},
            onAppClick = {},
            modifier = Modifier.padding(MyNotiDimens.screenHorizontal)
        )
    }
}
