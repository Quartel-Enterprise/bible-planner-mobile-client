package com.quare.bibleplanner.feature.day.presentation.content.loaded

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.LoadedDayLandscapeScreenContent
import com.quare.bibleplanner.feature.day.presentation.content.loaded.portrait.LoadedDayPortraitScreenContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

private const val WIDE_SCREEN_BREAKPOINT = 600
private const val MAX_CONTENT_WIDTH = 1200

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun LoadedDayContent(
    modifier: Modifier = Modifier,
    uiState: DayUiState.Loaded,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onEvent: (DayUiEvent) -> Unit,
) {
    val day = uiState.day
    BoxWithConstraints(modifier = modifier) {
        val isWideScreen = maxWidth > WIDE_SCREEN_BREAKPOINT.dp
        val contentMaxWidth = maxWidth.coerceAtMost(MAX_CONTENT_WIDTH.dp)

        if (isWideScreen) {
            LoadedDayLandscapeScreenContent(
                contentMaxWidth = contentMaxWidth,
                day = day,
                uiState = uiState,
                onEvent = onEvent,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        } else {
            LoadedDayPortraitScreenContent(
                day = day,
                onEvent = onEvent,
                uiState = uiState,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        }
    }
}
