package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.ui.icons.AppIcon
import org.jetbrains.compose.resources.StringResource

data class OverflowOptionPresentationModel(
    val name: StringResource,
    val type: OverflowOption,
    val icon: AppIcon,
)
