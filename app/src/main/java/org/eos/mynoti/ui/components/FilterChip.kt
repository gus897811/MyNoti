package org.eos.mynoti.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import org.eos.mynoti.ui.theme.MyNotiChipShape
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
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = MyNotiDimens.filterMinHeight),
        shape = MyNotiChipShape,
        color = if (selected) colorScheme.primary else colorScheme.surface,
        contentColor = if (selected) colorScheme.onPrimary else colorScheme.onSurface,
        border = if (selected) {
            null
        } else {
            BorderStroke(1.dp, colorScheme.outline)
        }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = MyNotiDimens.spaceSm
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.spaceSm)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(MyNotiDimens.spaceLg)
                )
            }
            Text(
                text = label,
                style = MyNotiTextStyles.metadata
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipPreview() {
    MyNotiTheme {
        Row(
            modifier = Modifier.padding(MyNotiDimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing)
        ) {
            FilterChip(label = "All", selected = true, onClick = {}, icon = Icons.Outlined.Apps)
            FilterChip(label = "Important", selected = false, onClick = {})
        }
    }
}
