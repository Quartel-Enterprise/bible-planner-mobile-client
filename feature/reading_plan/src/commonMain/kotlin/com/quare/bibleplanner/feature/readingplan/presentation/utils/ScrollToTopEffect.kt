package com.quare.bibleplanner.feature.readingplan.presentation.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import kotlinx.coroutines.delay

@Composable
internal fun ScrollToTopEffect(
    scrollToTop: Boolean,
    lazyListState: LazyListState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            lazyListState.animateScrollToItem(0, scrollOffset = 0)
            delay(400)
            onEvent(ReadingPlanUiEvent.OnScrollToTopCompleted)
        }
    }
}
