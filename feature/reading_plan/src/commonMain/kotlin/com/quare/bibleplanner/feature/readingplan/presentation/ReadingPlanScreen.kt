package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.component.ReadingPlanTopBar
import com.quare.bibleplanner.feature.readingplan.presentation.component.fabs.ReadingPlanFabsComponent
import com.quare.bibleplanner.feature.readingplan.presentation.content.ReadingPlanContent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState

private const val MAX_CONTENT_WIDTH = 600

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun ReadingPlanScreen(
    uiState: ReadingPlanUiState,
    lazyListState: LazyListState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    snackbarHostState: SnackbarHostState,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ReadingPlanTopBar(
                scrollBehavior = scrollBehavior,
                isShowingMenu = uiState.isShowingMenu,
                onEvent = onEvent,
            )
        },
        floatingActionButton = {
            ReadingPlanFabsComponent(uiState, onEvent)
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val constrainedWidth = maxWidth.coerceAtMost(MAX_CONTENT_WIDTH.dp)
            ReadingPlanContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onEvent = onEvent,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                maxContentWidth = constrainedWidth,
                lazyListState = lazyListState,
            )
        }
    }
}
