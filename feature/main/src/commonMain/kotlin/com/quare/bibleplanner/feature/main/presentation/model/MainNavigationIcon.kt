package com.quare.bibleplanner.feature.main.presentation.model

import com.quare.bibleplanner.core.profile.domain.model.AvatarSource

sealed interface MainNavigationIcon {
    // Why: each UI draws the tab's own icon for its route, so the state carries no Compose or UIKit type.
    data object Default : MainNavigationIcon

    data class Profile(
        val avatar: AvatarSource,
        val displayName: String?,
    ) : MainNavigationIcon
}
