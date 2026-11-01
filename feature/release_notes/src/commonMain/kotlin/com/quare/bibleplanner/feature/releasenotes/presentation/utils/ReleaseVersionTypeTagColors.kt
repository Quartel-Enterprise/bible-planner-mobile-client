package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType

internal fun ReleaseVersionType.isUpcoming(): Boolean = this == ReleaseVersionType.UPCOMING

@Composable
internal fun ReleaseVersionType.tagBackgroundColor(): Color = if (isUpcoming()) {
    MaterialTheme.colorScheme.tertiaryContainer
} else {
    MaterialTheme.colorScheme.primaryContainer
}

@Composable
internal fun ReleaseVersionType.tagContentColor(): Color = if (isUpcoming()) {
    MaterialTheme.colorScheme.onTertiaryContainer
} else {
    MaterialTheme.colorScheme.onPrimaryContainer
}
