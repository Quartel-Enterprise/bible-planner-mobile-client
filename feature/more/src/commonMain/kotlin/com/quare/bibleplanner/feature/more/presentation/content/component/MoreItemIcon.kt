package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.more.presentation.model.MoreIcon
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MoreItemIcon(
    icon: MoreIcon,
    contentDescription: String?,
    iconColor: Color,
) {
    when (icon) {
        is MoreIcon.DrawableResourceIcon -> {
            Icon(
                painter = painterResource(icon.resource),
                contentDescription = contentDescription,
                tint = iconColor,
            )
        }

        is MoreIcon.ImageVectorIcon -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                tint = iconColor,
            )
        }
    }
}
