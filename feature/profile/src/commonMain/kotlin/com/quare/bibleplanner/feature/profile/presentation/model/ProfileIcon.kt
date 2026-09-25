package com.quare.bibleplanner.feature.profile.presentation.model

import com.quare.bibleplanner.ui.icons.AppIcon
import org.jetbrains.compose.resources.DrawableResource

internal sealed interface ProfileIcon {
    data class SystemIcon(
        val icon: AppIcon,
    ) : ProfileIcon

    data class DrawableResourceIcon(
        val resource: DrawableResource,
    ) : ProfileIcon
}
