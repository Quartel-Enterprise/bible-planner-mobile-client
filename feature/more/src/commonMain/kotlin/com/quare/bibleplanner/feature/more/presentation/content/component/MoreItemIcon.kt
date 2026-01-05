package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.more.presentation.model.MoreIcon
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MoreItemIcon(
    icon: MoreIcon,
    contentDescription: String?,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    when (icon) {
        is MoreIcon.DrawableResourceIcon -> {
            Icon(
                painter = painterResource(icon.resource),
                contentDescription = contentDescription,
                tint = tint,
            )
        }

        is MoreIcon.ImageVectorIcon -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                tint = tint,
            )
        }
    }
}
