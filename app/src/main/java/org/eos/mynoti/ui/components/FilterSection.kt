package org.eos.mynoti.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles

@Composable
fun FilterSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MyNotiTextStyles.caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = MyNotiDimens.screenHorizontal,
                vertical = MyNotiDimens.spaceXs
            )
        )
        content()
    }
}

@Composable
fun <T> FilterChipRow(
    items: List<T>,
    label: (T) -> String,
    selected: (T) -> Boolean,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: ((T) -> ImageVector?)? = null,
    key: ((T) -> Any)? = null
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = MyNotiDimens.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing)
    ) {
        if (key != null) {
            items(items = items, key = key) { item ->
                FilterChip(
                    label = label(item),
                    selected = selected(item),
                    onClick = { onClick(item) },
                    icon = icon?.invoke(item)
                )
            }
        } else {
            items(items = items) { item ->
                FilterChip(
                    label = label(item),
                    selected = selected(item),
                    onClick = { onClick(item) },
                    icon = icon?.invoke(item)
                )
            }
        }
    }
}
