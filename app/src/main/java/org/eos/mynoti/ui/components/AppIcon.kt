package org.eos.mynoti.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.tooling.preview.Preview
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.ui.theme.BankGreen
import org.eos.mynoti.ui.theme.CardNavy
import org.eos.mynoti.ui.theme.HeyYoungGreen
import org.eos.mynoti.ui.theme.KakaoBlack
import org.eos.mynoti.ui.theme.KakaoYellow
import org.eos.mynoti.ui.theme.LearningXBlue
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiIconShape
import org.eos.mynoti.ui.theme.MyNotiTheme
import org.eos.mynoti.ui.theme.PrimaryBlue

data class AppIconStyle(
    val background: Color,
    val content: Color,
    val icon: ImageVector
)

fun appIconStyle(appPackageName: String): AppIconStyle {
    return when (appPackageName) {
        AppPackages.LEARNING_X -> AppIconStyle(
            background = LearningXBlue,
            content = Color.White,
            icon = Icons.Outlined.School
        )
        AppPackages.HEY_YOUNG -> AppIconStyle(
            background = HeyYoungGreen,
            content = Color.White,
            icon = Icons.Outlined.School
        )
        AppPackages.KAKAOTALK -> AppIconStyle(
            background = KakaoYellow,
            content = KakaoBlack,
            icon = Icons.Outlined.ChatBubble
        )
        AppPackages.SHINHAN_CARD -> AppIconStyle(
            background = CardNavy,
            content = Color.White,
            icon = Icons.Outlined.CreditCard
        )
        AppPackages.KAKAOBANK -> AppIconStyle(
            background = BankGreen,
            content = Color.White,
            icon = Icons.Outlined.AccountBalance
        )
        else -> AppIconStyle(
            background = PrimaryBlue,
            content = Color.White,
            icon = Icons.Outlined.Notifications
        )
    }
}

@Composable
fun AppIcon(
    appPackageName: String,
    modifier: Modifier = Modifier,
    size: Dp = MyNotiDimens.appIcon,
    contentDescription: String? = null
) {
    val style = appIconStyle(appPackageName)
    Box(
        modifier = modifier
            .size(size)
            .clip(MyNotiIconShape)
            .background(style.background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = contentDescription,
            tint = style.content,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppIconPreview() {
    MyNotiTheme {
        Row(
            modifier = Modifier.padding(MyNotiDimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(MyNotiDimens.spaceSm)
        ) {
            AppIcon(appPackageName = AppPackages.LEARNING_X, contentDescription = "LearningX")
            AppIcon(appPackageName = AppPackages.KAKAOTALK, contentDescription = "KakaoTalk")
            AppIcon(appPackageName = AppPackages.HEY_YOUNG, contentDescription = "HeyYoung")
        }
    }
}
