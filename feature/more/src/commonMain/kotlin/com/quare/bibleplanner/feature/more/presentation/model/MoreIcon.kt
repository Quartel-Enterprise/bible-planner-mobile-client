package com.quare.bibleplanner.feature.more.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.DrawableResource

internal sealed interface MoreIcon {
    data class ImageVectorIcon(
        val imageVector: ImageVector,
    ) : MoreIcon

    data class DrawableResourceIcon(
        val resource: DrawableResource,
    ) : MoreIcon
}
