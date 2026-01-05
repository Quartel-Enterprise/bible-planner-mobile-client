package com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side.LoadedDayLandscapeScreenLeftContent
import com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side.LoadedDayLandscapeScreenRightContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun LoadedDayLandscapeScreenContent(
    contentMaxWidth: Dp,
    day: DayModel,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.width(contentMaxWidth),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier.weight(0.4f),
                    ) {
                        LoadedDayLandscapeScreenLeftContent(
                            day = day,
                            uiState = uiState,
                            onEvent = onEvent,
                        )
                    }
                    Box(
                        modifier = Modifier.weight(0.6f),
                    ) {
                        LoadedDayLandscapeScreenRightContent(
                            day = day,
                            uiState = uiState,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope,
                            onEvent = onEvent,
                        )
                    }
                }
            }
        }
    }
}
