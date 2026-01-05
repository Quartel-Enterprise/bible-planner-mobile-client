package com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side.LoadedDayLandscapeScreenLeftContent
import com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side.LoadedDayLandscapeScreenRightContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope

@OptIn(ExperimentalSharedTransitionApi::class)
internal fun ResponsiveContentScope.loadedDayLandscapeScreenContent(
    day: DayModel,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    responsiveItem {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.weight(0.4f),
            ) {
                LoadedDayLandscapeScreenLeftContent(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
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
