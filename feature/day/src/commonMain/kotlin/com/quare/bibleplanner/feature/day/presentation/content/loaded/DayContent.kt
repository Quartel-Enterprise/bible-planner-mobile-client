package com.quare.bibleplanner.feature.day.presentation.content.loaded

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.presentation.content.loading.LoadingDayContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun DayContent(
    uiState: DayUiState,
    onEvent: (DayUiEvent) -> Unit,
    platform: Platform,
    isLandscape: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is DayUiState.Loading -> LoadingDayContent(
            modifier = modifier,
            isLandscape = isLandscape,
            platform = platform,
            onEvent = onEvent,
        )

        is DayUiState.Loaded -> LoadedDayContent(
            modifier = modifier,
            uiState = uiState,
            onEvent = onEvent,
            platform = platform,
            isLandscape = isLandscape,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
}
