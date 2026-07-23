package com.quare.bibleplanner.feature.day.presentation.content.loaded

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.LoadedDayLandscapeContent
import com.quare.bibleplanner.feature.day.presentation.content.loaded.portrait.loadedDayPortraitScreenContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.ResponsiveColumn

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun LoadedDayContent(
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
    platform: Platform,
    isLandscape: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
) {
    val day = uiState.day
    if (isLandscape) {
        LoadedDayLandscapeContent(
            modifier = modifier,
            day = day,
            uiState = uiState,
            onEvent = onEvent,
            platform = platform,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    } else {
        ResponsiveColumn(
            modifier = modifier,
            contentPadding = PaddingValues(bottom = 16.dp),
            portraitContent = {
                loadedDayPortraitScreenContent(
                    day = day,
                    onEvent = onEvent,
                    uiState = uiState,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
            },
        )
    }
}
