package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.component.DayScreenTopBarComponent
import com.quare.bibleplanner.feature.day.presentation.content.DayContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

private const val MAX_CONTENT_WIDTH = 600

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun DayScreen(
    uiState: DayUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onEvent: (DayUiEvent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DayScreenTopBarComponent(
                uiState = uiState,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                scrollBehavior = scrollBehavior,
                onEvent = onEvent,
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val constrainedWidth = maxWidth.coerceAtMost(MAX_CONTENT_WIDTH.dp)
            DayContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onEvent = onEvent,
                maxContentWidth = constrainedWidth,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        }
    }
}
