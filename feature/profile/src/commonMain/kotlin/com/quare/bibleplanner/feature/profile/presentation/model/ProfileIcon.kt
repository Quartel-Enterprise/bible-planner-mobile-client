package com.quare.bibleplanner.feature.profile.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.DrawableResource

internal sealed interface ProfileIcon {
    data class ImageVectorIcon(
        val imageVector: ImageVector,
    ) : ProfileIcon

    data class DrawableResourceIcon(
        val resource: DrawableResource,
    ) : ProfileIcon
}
