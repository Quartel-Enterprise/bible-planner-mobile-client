package com.quare.bibleplanner.feature.readingplan.presentation.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScrollToTopObserver(
    scrollToTop: Boolean,
    lazyListState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            // Scroll to top
            lazyListState.animateScrollToItem(0, scrollOffset = 0)
            // Wait for scroll animation to complete
            delay(400)
            // Ensure we're at the top, then force top bar to show
            if (lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0) {
                scrollBehavior.state.heightOffset = 0f
            }
            onEvent(ReadingPlanUiEvent.OnScrollToTopCompleted)
        }
    }
}
