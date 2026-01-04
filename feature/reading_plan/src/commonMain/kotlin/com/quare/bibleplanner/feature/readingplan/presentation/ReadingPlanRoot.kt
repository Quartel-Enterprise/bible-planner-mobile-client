package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.ui.utils.MainScaffoldState
import com.quare.bibleplanner.feature.readingplan.presentation.component.ReadingPlanTopBar
import com.quare.bibleplanner.feature.readingplan.presentation.component.fabs.ReadingPlanFabsComponent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.utils.ReadingPlanUiActionCollector
import com.quare.bibleplanner.feature.readingplan.presentation.utils.ScrollToTopObserver
import com.quare.bibleplanner.feature.readingplan.presentation.utils.ScrollToWeekAction
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.readingPlan(
    navController: NavController,
    mainScaffoldState: MainScaffoldState,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<BottomNavRoute.Plans> {
        val viewModel = koinViewModel<ReadingPlanViewModel>()
        val onEvent = viewModel::onEvent
        val uiState by viewModel.uiState.collectAsState()
        val lazyListState = rememberLazyListState()
        val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val snackbarHostState = mainScaffoldState.snackbarHostState

        // Update MainScaffoldState with local UI components
        SideEffect {
            mainScaffoldState.setTopBar {
                ReadingPlanTopBar(
                    scrollBehavior = topAppBarScrollBehavior,
                    isShowingMenu = uiState.isShowingMenu,
                    onEvent = onEvent,
                )
            }
            mainScaffoldState.setFab {
                ReadingPlanFabsComponent(uiState, onEvent)
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                mainScaffoldState.clearTopBar()
                mainScaffoldState.clearFab()
            }
        }

        ReadingPlanScreenObserver(
            lazyListState = lazyListState,
            uiActionFlow = viewModel.uiAction,
            snackbarHostState = snackbarHostState,
            scrollBehavior = topAppBarScrollBehavior,
            uiState = uiState,
            navController = navController,
            onEvent = onEvent,
        )
        ReadingPlanScreen(
            uiState = uiState,
            lazyListState = lazyListState,
            topAppBarScrollBehavior = topAppBarScrollBehavior,
            onEvent = onEvent,
            animatedContentScope = this,
            sharedTransitionScope = sharedTransitionScope,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingPlanScreenObserver(
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
    uiActionFlow: Flow<ReadingPlanUiAction>,
    scrollBehavior: TopAppBarScrollBehavior,
    uiState: ReadingPlanUiState,
    navController: NavController,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val scrollToTop = uiState.scrollToTop
    val scrollToWeekNumber = uiState.scrollToWeekNumber
    val loadedUiState = remember(uiState) { uiState as? ReadingPlanUiState.Loaded }
    // Track scroll position to determine if scroll-to-top FAB should be visible
    // Also ensure top bar shows when at top
    LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
        val isScrolled = lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        onEvent(ReadingPlanUiEvent.OnScrollStateChange(isScrolled))

        // When at top, ensure top bar is shown
        if (!isScrolled) {
            scrollBehavior.state.heightOffset = 0f
        }
    }

    ScrollToTopObserver(
        scrollToTop = scrollToTop,
        lazyListState = lazyListState,
        scrollBehavior = scrollBehavior,
        onEvent = onEvent,
    )

    ScrollToWeekAction(
        scrollToWeekNumber = scrollToWeekNumber,
        loadedUiState = loadedUiState,
        lazyListState = lazyListState,
        onEvent = onEvent,
    )

    ReadingPlanUiActionCollector(
        snackbarHostState = snackbarHostState,
        flow = uiActionFlow,
        navController = navController,
    )
}
