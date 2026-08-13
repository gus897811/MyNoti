package org.eos.mynoti.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.eos.mynoti.R
import org.eos.mynoti.ui.theme.MyNotiBadgeShape
import org.eos.mynoti.ui.theme.MyNotiCardShape
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeywordChipGroup(
    keywords: List<String>,
    onAddClick: () -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MyNotiCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        FlowRow(
            modifier = Modifier.padding(MyNotiDimens.spaceLg),
            horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing),
            verticalArrangement = Arrangement.spacedBy(MyNotiDimens.chipSpacing)
        ) {
            keywords.forEach { keyword ->
                KeywordChip(
                    text = keyword,
                    onRemove = { onRemove(keyword) }
                )
            }
            Surface(
                onClick = onAddClick,
                shape = MyNotiBadgeShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = MyNotiDimens.spaceMd,
                        vertical = MyNotiDimens.spaceSm
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.spaceXs)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(MyNotiDimens.spaceLg)
                    )
                    Text(
                        text = stringResource(R.string.add_keyword),
                        style = MyNotiTextStyles.metadata,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun KeywordChip(
    text: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MyNotiBadgeShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MyNotiTextStyles.metadata,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = MyNotiDimens.spaceMd)
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(MyNotiDimens.minTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.cd_remove_keyword, text),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(MyNotiDimens.spaceLg)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KeywordChipGroupPreview() {
    MyNotiTheme {
        KeywordChipGroup(
            keywords = listOf("과제", "시험", "마감"),
            onAddClick = {},
            onRemove = {},
            modifier = Modifier.padding(MyNotiDimens.screenHorizontal)
        )
    }
}
