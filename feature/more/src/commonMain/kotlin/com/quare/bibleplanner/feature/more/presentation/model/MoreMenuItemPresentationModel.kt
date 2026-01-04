package com.quare.bibleplanner.feature.more.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

internal data class MoreMenuItemPresentationModel(
    val name: StringResource,
    val icon: ImageVector,
    val event: MoreUiEvent,
)
