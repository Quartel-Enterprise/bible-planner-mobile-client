package com.quare.bibleplanner.feature.profile.presentation.content.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileIcon
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ProfileItemIcon(
    icon: ProfileIcon,
    contentDescription: String?,
    iconColor: Color,
) {
    when (icon) {
        is ProfileIcon.DrawableResourceIcon -> {
            Icon(
                painter = painterResource(icon.resource),
                contentDescription = contentDescription,
                tint = iconColor,
            )
        }

        is ProfileIcon.ImageVectorIcon -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                tint = iconColor,
            )
        }
    }
}
