package com.quare.bibleplanner.feature.day.presentation.content.loaded

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.loadedDayLandscapeScreenContent
import com.quare.bibleplanner.feature.day.presentation.content.loaded.portrait.loadedDayPortraitScreenContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.ResponsiveContent

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
    ResponsiveContent(
        modifier = modifier,
        portraitContent = { contentMaxWidth ->
            loadedDayPortraitScreenContent(
                contentMaxWidth = contentMaxWidth,
                day = day,
                onEvent = onEvent,
                uiState = uiState,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        },
        landscapeContent = { contentMaxWidth ->
            loadedDayLandscapeScreenContent(
                contentMaxWidth = contentMaxWidth,
                day = day,
                uiState = uiState,
                onEvent = onEvent,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        },
    )
}
