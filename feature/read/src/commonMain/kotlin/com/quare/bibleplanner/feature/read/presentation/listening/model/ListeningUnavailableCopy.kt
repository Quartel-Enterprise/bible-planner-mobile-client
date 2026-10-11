package com.quare.bibleplanner.feature.read.presentation.listening.model

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

internal data class ListeningUnavailableCopy(
    val icon: ImageVector,
    val title: StringResource,
    val body: StringResource,
    val action: StringResource?,
)
