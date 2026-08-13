package org.eos.mynoti.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object MyNotiDimens {
    val screenHorizontal = 20.dp
    val screenVertical = 8.dp
    val sectionSpacing = 12.dp
    val itemSpacing = 10.dp
    val chipSpacing = 8.dp

    val cardPadding = 16.dp
    val cardRadius = 18.dp
    val chipRadius = 24.dp
    val badgeRadius = 20.dp
    val iconRadius = 12.dp

    val appIcon = 44.dp
    val appIconSmall = 36.dp
    val appIconLarge = 48.dp
    val unreadDot = 7.dp
    val importantAccent = 4.dp
    val minTouchTarget = 48.dp
    val filterMinHeight = 40.dp

    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val spaceLg = 16.dp
    val spaceXl = 20.dp
    val spaceXxl = 24.dp
}

val MyNotiCardShape = RoundedCornerShape(MyNotiDimens.cardRadius)
val MyNotiChipShape = RoundedCornerShape(MyNotiDimens.chipRadius)
val MyNotiBadgeShape = RoundedCornerShape(MyNotiDimens.badgeRadius)
val MyNotiIconShape = RoundedCornerShape(MyNotiDimens.iconRadius)
