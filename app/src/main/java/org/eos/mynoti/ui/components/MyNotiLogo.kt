package org.eos.mynoti.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.eos.mynoti.R
import org.eos.mynoti.ui.theme.MyNotiDimens

@Composable
fun MyNotiLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = stringResource(R.string.app_name)
) {
    val painter = painterResource(R.drawable.ic_mynoti_logo)
    val intrinsic = painter.intrinsicSize
    val aspectRatio = if (intrinsic.height > 0f) {
        intrinsic.width / intrinsic.height
    } else {
        2.18f
    }
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier
            .height(MyNotiDimens.appLogoHeight)
            .aspectRatio(aspectRatio),
        contentScale = ContentScale.Fit
    )
}
