package com.quare.bibleplanner.feature.readingplan.presentation.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import kotlinx.coroutines.delay

@Composable
internal fun ScrollToWeekAction(
    scrollToWeekNumber: Int,
    loadedUiState: ReadingPlanUiState.Loaded?,
    lazyListState: LazyListState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    LaunchedEffect(scrollToWeekNumber, loadedUiState) {
        if (scrollToWeekNumber > 0 && loadedUiState != null) {
            // Special handling for week 1 (first week at top)
            if (scrollToWeekNumber == 1) {
                // Check if we're already at the top
                val isAtTop = lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset == 0

                if (!isAtTop) {
                    // Scroll to top since we're not there yet
                    delay(100)
                    lazyListState.animateScrollToItem(0, scrollOffset = 0)
                }

                // Mark as completed (whether we scrolled or not)
                onEvent(ReadingPlanUiEvent.OnScrollToWeekCompleted)
                return@LaunchedEffect
            }

            // Find the index of the week in the list
            val weekIndex = loadedUiState.weekPlans.indexOfFirst {
                it.weekPlan.number == scrollToWeekNumber
            }

            if (weekIndex >= 0) {
                // Add 2 for the header items (PlanTypesSegmentedButtons and PlanProgress)
                val targetIndex = weekIndex + 2

                // Small delay to ensure layout is ready
                delay(100)

                lazyListState.animateScrollToItem(
                    index = targetIndex,
                    scrollOffset = 0,
                )

                // Notify that scrolling is completed
                onEvent(ReadingPlanUiEvent.OnScrollToWeekCompleted)
            }
        }
    }
}
