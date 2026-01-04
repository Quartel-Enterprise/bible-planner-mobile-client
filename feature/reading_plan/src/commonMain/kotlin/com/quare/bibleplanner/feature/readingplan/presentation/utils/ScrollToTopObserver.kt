package com.quare.bibleplanner.feature.readingplan.presentation.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import kotlinx.coroutines.delay

@Composable
internal fun ScrollToTopObserver(
    scrollToTop: Boolean,
    lazyListState: LazyListState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            // Scroll to top
            lazyListState.animateScrollToItem(0, scrollOffset = 0)
            // Wait for scroll animation to complete
            delay(400)
            onEvent(ReadingPlanUiEvent.OnScrollToTopCompleted)
        }
    }
}
