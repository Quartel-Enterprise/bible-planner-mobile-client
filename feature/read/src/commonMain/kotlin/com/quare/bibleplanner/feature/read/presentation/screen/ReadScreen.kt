package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState

@Composable
internal fun ReadScreen(
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    BoxWithConstraints {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            ReadLandscapeScreen(
                state = state,
                onEvent = onEvent,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        } else {
            ReadPortraitScreen(
                state = state,
                onEvent = onEvent,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
}
