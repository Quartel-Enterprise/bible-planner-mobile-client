package com.quare.bibleplanner.feature.profile.presentation.model

import org.jetbrains.compose.resources.StringResource

internal data class ProfileMenuItemPresentationModel(
    val name: StringResource,
    val subtitle: StringResource? = null,
    val icon: ProfileIcon,
    val type: ProfileOptionItemType,
)
