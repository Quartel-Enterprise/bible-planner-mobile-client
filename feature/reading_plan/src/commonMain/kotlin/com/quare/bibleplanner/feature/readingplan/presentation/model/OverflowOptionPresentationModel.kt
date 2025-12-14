package com.quare.bibleplanner.feature.readingplan.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

data class OverflowOptionPresentationModel(
    val name: StringResource,
    val type: OverflowOption,
    val icon: ImageVector,
)
