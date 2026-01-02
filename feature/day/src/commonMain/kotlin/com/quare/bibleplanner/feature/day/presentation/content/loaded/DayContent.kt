package com.quare.bibleplanner.feature.day.presentation.content.loaded

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.day.presentation.content.loading.LoadingDayContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun DayContent(
    uiState: DayUiState,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    when (uiState) {
        is DayUiState.Loading -> LoadingDayContent(modifier)

        is DayUiState.Loaded -> LoadedDayContent(
            modifier = modifier,
            uiState = uiState,
            onEvent = onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
}
