package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType

@Composable
fun ReleaseVersionType.tagBackgroundColor(): Color = if (this == ReleaseVersionType.UPCOMING) {
    MaterialTheme.colorScheme.tertiaryContainer
} else {
    MaterialTheme.colorScheme.primaryContainer
}

@Composable
fun ReleaseVersionType.tagContentColor(): Color = if (this == ReleaseVersionType.UPCOMING) {
    MaterialTheme.colorScheme.onTertiaryContainer
} else {
    MaterialTheme.colorScheme.onPrimaryContainer
}
