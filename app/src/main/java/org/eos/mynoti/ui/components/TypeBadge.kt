package org.eos.mynoti.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import org.eos.mynoti.domain.model.NotificationType
import org.eos.mynoti.ui.theme.MyNotiBadgeShape
import org.eos.mynoti.ui.theme.MyNotiDimens
import org.eos.mynoti.ui.theme.MyNotiTextStyles
import org.eos.mynoti.ui.theme.MyNotiTheme
import org.eos.mynoti.ui.theme.TypeAssignment
import org.eos.mynoti.ui.theme.TypeClass
import org.eos.mynoti.ui.theme.TypeCommunication
import org.eos.mynoti.ui.theme.TypeEtc
import org.eos.mynoti.ui.theme.TypeFinancial

fun NotificationType.icon(): ImageVector = when (this) {
    NotificationType.CLASS -> Icons.Outlined.School
    NotificationType.ASSIGNMENT -> Icons.AutoMirrored.Outlined.Assignment
    NotificationType.COMMUNICATION -> Icons.Outlined.ChatBubbleOutline
    NotificationType.FINANCIAL -> Icons.Outlined.AccountBalance
    NotificationType.ETC -> Icons.Outlined.MoreHoriz
}

fun NotificationType.accentColor(): Color = when (this) {
    NotificationType.CLASS -> TypeClass
    NotificationType.ASSIGNMENT -> TypeAssignment
    NotificationType.COMMUNICATION -> TypeCommunication
    NotificationType.FINANCIAL -> TypeFinancial
    NotificationType.ETC -> TypeEtc
}

@Composable
fun TypeBadge(
    type: NotificationType,
    modifier: Modifier = Modifier
) {
    val color = type.accentColor()
    Surface(
        modifier = modifier,
        shape = MyNotiBadgeShape,
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = type.label,
            style = MyNotiTextStyles.caption,
            color = color,
            modifier = Modifier.padding(
                horizontal = MyNotiDimens.spaceSm,
                vertical = MyNotiDimens.spaceXs
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TypeBadgePreview() {
    MyNotiTheme {
        TypeBadge(
            type = NotificationType.ASSIGNMENT,
            modifier = Modifier.padding(MyNotiDimens.spaceMd)
        )
    }
}
