package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanProgress
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanTypesSegmentedButtons
import com.quare.bibleplanner.feature.readingplan.presentation.component.WeekPlanItem
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingPlanContent(
    modifier: Modifier = Modifier,
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    maxContentWidth: Dp,
    scrollToWeekNumber: Int,
    onScrollToWeekCompleted: () -> Unit,
    scrollToTop: Boolean,
    onScrollToTopCompleted: () -> Unit,
    onScrollStateChange: (Boolean) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val loadedUiState = uiState as? ReadingPlanUiState.Loaded
    val lazyListState = rememberLazyListState()

    // Track scroll position to determine if scroll-to-top FAB should be visible
    // Also ensure top bar shows when at top
    androidx.compose.runtime.LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
        val isScrolled = lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        onScrollStateChange(isScrolled)
        
        // When at top, ensure top bar is shown
        if (!isScrolled) {
            scrollBehavior.state.heightOffset = 0f
        }
    }

    // Handle scroll to top action
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
            onScrollToTopCompleted()
        }
    }

    // Scroll to the specified week when scrollToWeekNumber changes or when data loads
    LaunchedEffect(scrollToWeekNumber, loadedUiState) {
        if (scrollToWeekNumber > 0 && loadedUiState != null) {
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
                onScrollToWeekCompleted()
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(maxContentWidth)) {
                    PlanTypesSegmentedButtons(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        selectedReadingPlan = uiState.selectedReadingPlan,
                        onPlanClick = {
                            onEvent(ReadingPlanUiEvent.OnPlanClick(it))
                        },
                    )
                }
            }
            VerticalSpacer()
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(maxContentWidth)) {
                    PlanProgress(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        progress = loadedUiState?.progress ?: 0f,
                        isLoading = uiState is ReadingPlanUiState.Loading,
                    )
                }
            }
            VerticalSpacer()
        }
        when (uiState) {
            is ReadingPlanUiState.Loaded -> items(
                items = uiState.weekPlans,
                key = { weekPresentation -> weekPresentation.weekPlan.number },
            ) { weekPresentation ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.width(maxContentWidth)) {
                        WeekPlanItem(
                            modifier = Modifier.fillMaxWidth(),
                            weekPresentation = weekPresentation,
                            onEvent = onEvent,
                        )
                    }
                }
            }

            is ReadingPlanUiState.Loading -> item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.width(maxContentWidth)) {
                        LoadingReadingPlanContent(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
