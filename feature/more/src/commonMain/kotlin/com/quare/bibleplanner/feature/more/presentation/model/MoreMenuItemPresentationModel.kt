package com.quare.bibleplanner.feature.more.presentation.model

import org.jetbrains.compose.resources.StringResource

internal data class MoreMenuItemPresentationModel(
    val name: StringResource,
    val subtitle: StringResource? = null,
    val icon: MoreIcon,
    val type: MoreOptionItemType,
)
