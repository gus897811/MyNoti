package org.eos.mynoti.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.eos.mynoti.R
import org.eos.mynoti.ui.theme.ImportantBackground
import org.eos.mynoti.ui.theme.ImportantAccent
import org.eos.mynoti.ui.theme.MyNotiBadgeShape
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme

@Composable
fun ImportanceBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MyNotiBadgeShape,
        color = ImportantBackground
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MyNotiDimens.spaceSm,
                vertical = MyNotiDimens.spaceXs
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Whatshot,
                contentDescription = null,
                tint = ImportantAccent,
                modifier = Modifier
                    .padding(end = MyNotiDimens.spaceXs)
                    .size(MyNotiDimens.spaceMd)
            )
            Text(
                text = stringResource(R.string.importance),
                style = MyNotiTextStyles.caption,
                color = ImportantAccent
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportanceBadgePreview() {
    MyNotiTheme {
        ImportanceBadge(modifier = Modifier.padding(MyNotiDimens.spaceMd))
    }
}
