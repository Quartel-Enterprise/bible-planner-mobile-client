package com.quare.bibleplanner.feature.main.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource

sealed interface MainNavigationIcon {
    data class Vector(
        val imageVector: ImageVector,
    ) : MainNavigationIcon

    data class Profile(
        val avatar: AvatarSource,
        val displayName: String?,
    ) : MainNavigationIcon
}
