package org.eos.mynoti.ui.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.TargetApp
import org.eos.mynoti.ui.components.AppIcon
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppFilterSheet(
    apps: List<TargetApp>,
    selectedApps: Set<String>,
    onToggleApp: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        HomeAppFilterSheetContent(
            apps = apps,
            selectedApps = selectedApps,
            onToggleApp = onToggleApp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = MyNotiDimens.screenHorizontal)
        )
    }
}

@Composable
fun HomeAppFilterSheetContent(
    apps: List<TargetApp>,
    selectedApps: Set<String>,
    onToggleApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.filter_apps_picker_title),
            style = MyNotiTextStyles.sectionTitle,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
        Text(
            text = stringResource(R.string.filter_apps_picker_description),
            style = MyNotiTextStyles.notificationSummary,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MyNotiDimens.spaceMd))
        if (apps.isEmpty()) {
            Text(
                text = stringResource(R.string.filter_apps_picker_empty),
                style = MyNotiTextStyles.notificationSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = MyNotiDimens.spaceXl)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(bottom = MyNotiDimens.spaceLg)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    HomeAppFilterRow(
                        app = app,
                        checked = app.packageName in selectedApps,
                        onToggle = { onToggleApp(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeAppFilterRow(
    app: TargetApp,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MyNotiDimens.minTouchTarget)
            .clickable(onClick = onToggle)
            .padding(vertical = MyNotiDimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            appPackageName = app.packageName,
            size = MyNotiDimens.appIconSmall,
            contentDescription = app.name
        )
        Spacer(modifier = Modifier.width(MyNotiDimens.spaceMd))
        Text(
            text = app.name,
            style = MyNotiTextStyles.notificationTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
    }
}

@Preview(showBackground = true, name = "Home app filter sheet")
@Composable
private fun HomeAppFilterSheetPreview() {
    MyNotiTheme {
        HomeAppFilterSheetContent(
            apps = AppSettings.defaultTargetApps,
            selectedApps = setOf(AppPackages.LEARNING_X, AppPackages.KAKAOTALK),
            onToggleApp = {},
            modifier = Modifier.padding(MyNotiDimens.screenHorizontal)
        )
    }
}

@Preview(showBackground = true, name = "Home app filter sheet empty")
@Composable
private fun HomeAppFilterSheetEmptyPreview() {
    MyNotiTheme {
        HomeAppFilterSheetContent(
            apps = emptyList(),
            selectedApps = emptySet(),
            onToggleApp = {},
            modifier = Modifier.padding(MyNotiDimens.screenHorizontal)
        )
    }
}
