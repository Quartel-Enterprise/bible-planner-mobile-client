package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.ReadingPlanNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.readingPlan(navController: NavController) {
    composable<ReadingPlanNavRoute> {
        val viewModel = koinViewModel<ReadingPlanViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val lazyListState = rememberLazyListState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val loadedUiState = remember(uiState) { uiState as? ReadingPlanUiState.Loaded }

        // Track scroll position to determine if scroll-to-top FAB should be visible
        // Also ensure top bar shows when at top
        LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
            val isScrolled = lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
            viewModel.onEvent(ReadingPlanUiEvent.OnScrollStateChange(isScrolled))

            // When at top, ensure top bar is shown
            if (!isScrolled) {
                scrollBehavior.state.heightOffset = 0f
            }
        }

        // Handle scroll to top action
        LaunchedEffect(uiState.scrollToTop) {
            if (uiState.scrollToTop) {
                // Scroll to top
                lazyListState.animateScrollToItem(0, scrollOffset = 0)
                // Wait for scroll animation to complete
                delay(400)
                // Ensure we're at the top, then force top bar to show
                if (lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0) {
                    scrollBehavior.state.heightOffset = 0f
                }
                viewModel.onEvent(ReadingPlanUiEvent.OnScrollToTopCompleted)
            }
        }

        // Scroll to the specified week when scrollToWeekNumber changes or when data loads
        LaunchedEffect(uiState.scrollToWeekNumber, loadedUiState) {
            if (uiState.scrollToWeekNumber > 0 && loadedUiState != null) {
                // Special handling for week 1 (first week at top)
                if (uiState.scrollToWeekNumber == 1) {
                    // Check if we're already at the top
                    val isAtTop = lazyListState.firstVisibleItemIndex == 0 &&
                        lazyListState.firstVisibleItemScrollOffset == 0

                    if (!isAtTop) {
                        // Scroll to top since we're not there yet
                        delay(100)
                        lazyListState.animateScrollToItem(0, scrollOffset = 0)
                    }

                    // Mark as completed (whether we scrolled or not)
                    viewModel.onEvent(ReadingPlanUiEvent.OnScrollToWeekCompleted)
                    return@LaunchedEffect
                }

                // Find the index of the week in the list
                val weekIndex = loadedUiState.weekPlans.indexOfFirst {
                    it.weekPlan.number == uiState.scrollToWeekNumber
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
                    viewModel.onEvent(ReadingPlanUiEvent.OnScrollToWeekCompleted)
                }
            }
        }

        ActionCollector(viewModel.uiAction) { uiAction ->
            when (uiAction) {
                is ReadingPlanUiAction.GoToDay -> {
                    navController.navigate(
                        DayNavRoute(
                            dayNumber = uiAction.dayNumber,
                            weekNumber = uiAction.weekNumber,
                            readingPlanType = uiAction.readingPlanType.name,
                        ),
                    )
                }

                is ReadingPlanUiAction.ScrollToWeek -> {
                    // Scroll state is managed in ViewModel, no action needed here
                }

                ReadingPlanUiAction.ScrollToTop -> {
                    // Scroll state is managed in ViewModel, no action needed here
                }

                ReadingPlanUiAction.GoToDeleteAllProgress -> {
                    navController.navigate(DeleteAllProgressNavRoute)
                }

                ReadingPlanUiAction.GoToTheme -> {
                    navController.navigate(ThemeNavRoute)
                }
            }
        }
        ReadingPlanScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            lazyListState = lazyListState,
            scrollBehavior = scrollBehavior,
        )
    }
}
