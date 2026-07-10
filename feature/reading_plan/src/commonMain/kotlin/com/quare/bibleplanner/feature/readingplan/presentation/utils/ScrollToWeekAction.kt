package com.quare.bibleplanner.feature.readingplan.presentation.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.quare.bibleplanner.feature.readingplan.presentation.content.readingPlanWeekScrollIndex
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import kotlinx.coroutines.delay

private const val SCROLL_LAYOUT_SETTLE_MILLIS = 100L

@Composable
internal fun ScrollToWeekAction(
    scrollToWeekNumber: Int,
    scrollToWeekIsAutomatic: Boolean,
    loadedUiState: ReadingPlanUiState.Loaded?,
    includeSidePanel: Boolean,
    lazyListState: LazyListState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    LaunchedEffect(scrollToWeekNumber, loadedUiState) {
        if (scrollToWeekNumber <= 0 || loadedUiState == null) {
            return@LaunchedEffect
        }
        val skipAutomaticScroll = scrollToWeekIsAutomatic && includeSidePanel
        if (!skipAutomaticScroll) {
            val targetIndex = readingPlanWeekScrollIndex(
                state = loadedUiState,
                includeSidePanel = includeSidePanel,
                weekNumber = scrollToWeekNumber,
            )
            if (targetIndex >= 0) {
                delay(SCROLL_LAYOUT_SETTLE_MILLIS)
                lazyListState.animateScrollToItem(index = targetIndex)
            }
        }
        onEvent(ReadingPlanUiEvent.OnScrollToWeekCompleted)
    }
}
