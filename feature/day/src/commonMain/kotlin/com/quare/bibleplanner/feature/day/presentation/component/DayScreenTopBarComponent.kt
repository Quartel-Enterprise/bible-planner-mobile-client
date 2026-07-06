package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun DayScreenTopBarComponent(
    platform: Platform,
    uiState: DayUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (DayUiEvent) -> Unit,
) {
    TopAppBar(
        title = {
            when (uiState) {
                is DayUiState.Loaded -> DayHeaderTitle(
                    state = uiState,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )

                is DayUiState.Loading -> DayHeaderTitleSkeleton()
            }
        },
        navigationIcon = {
            BackIcon(platform = platform, onBackClick = { onEvent(DayUiEvent.OnBackClick) })
        },
        scrollBehavior = scrollBehavior,
    )
}
