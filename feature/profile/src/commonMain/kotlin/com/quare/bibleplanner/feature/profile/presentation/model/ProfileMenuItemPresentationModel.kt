package com.quare.bibleplanner.feature.profile.presentation.model

import org.jetbrains.compose.resources.StringResource

internal data class ProfileMenuItemPresentationModel(
    val name: StringResource,
    val subtitle: StringResource?,
    val icon: ProfileIcon,
    val type: ProfileOptionItemType,
)
