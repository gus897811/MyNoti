package org.eos.mynoti.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.eos.mynoti.R
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles

@Composable
fun MyNotiBrandHeader(
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(
            start = MyNotiDimens.spaceSm,
            end = MyNotiDimens.spaceSm,
            top = MyNotiDimens.screenVertical,
            bottom = MyNotiDimens.spaceSm
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MyNotiDimens.spaceMd)
        ) {
            Column {
                MyNotiLogo()
                Spacer(modifier = Modifier.height(MyNotiDimens.spaceXs))
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MyNotiTextStyles.notificationSummary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailing != null) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    trailing()
                }
            }
        }
    }
}
