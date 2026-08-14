package org.eos.mynoti.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.FilterChip as MaterialFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    MaterialFilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = MyNotiDimens.filterMinHeight),
        label = {
            Text(text = label, style = MyNotiTextStyles.metadata)
        },
        leadingIcon = icon?.let { image ->
            {
                Icon(
                    imageVector = image,
                    contentDescription = null,
                    modifier = Modifier.size(MyNotiDimens.spaceLg)
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    FilterChip(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = icon
    )
}

@Composable
fun TypeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    FilterChip(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = icon
    )
}

@Preview(showBackground = true)
@Composable
private fun FilterChipPreview() {
    MyNotiTheme {
        Row(
            modifier = Modifier.padding(MyNotiDimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing)
        ) {
            FilterChip(
                label = "All",
                selected = true,
                onClick = {},
                icon = Icons.Outlined.Apps
            )
            FilterChip(label = "중요", selected = false, onClick = {})
        }
    }
}
