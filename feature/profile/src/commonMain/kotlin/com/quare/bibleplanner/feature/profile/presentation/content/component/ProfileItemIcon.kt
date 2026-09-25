package com.quare.bibleplanner.feature.profile.presentation.content.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileIcon
import com.quare.bibleplanner.ui.icons.Icon
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

        is ProfileIcon.SystemIcon -> {
            Icon(
                icon = icon.icon,
                contentDescription = contentDescription,
                tint = iconColor,
            )
        }
    }
}
