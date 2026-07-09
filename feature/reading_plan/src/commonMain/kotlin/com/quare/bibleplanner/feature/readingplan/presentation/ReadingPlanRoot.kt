package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.readingplan.presentation.component.fabs.ReadingPlanFabsComponent
import com.quare.bibleplanner.feature.readingplan.presentation.content.ReadingPlanScreen
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.utils.ReadingPlanUiActionCollector
import com.quare.bibleplanner.feature.readingplan.presentation.utils.ScrollToTopObserver
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import com.quare.bibleplanner.ui.utils.MainTabScaffold
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun EntryProviderScope<NavKey>.readingPlan(
    onNavigate: (NavKey) -> Unit,
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    entry<BottomNavRoute.Plans> {
        ReadingPlanTabContent(
            onNavigate = onNavigate,
            navigationBar = navigationBar,
            navigationRail = navigationRail,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ReadingPlanTabContent(
    onNavigate: (NavKey) -> Unit,
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val viewModel = koinViewModel<ReadingPlanViewModel>()
    val onEvent = viewModel::onEvent
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    val snackbarHostState = LocalSnackbarHostState.current

    ReadingPlanScreenObserver(
        lazyListState = lazyListState,
        uiActionFlow = viewModel.uiAction,
        snackbarHostState = snackbarHostState,
        uiState = uiState,
        onNavigate = onNavigate,
        onEvent = onEvent,
    )
    MainTabScaffold(
        navigationBar = navigationBar,
        navigationRail = navigationRail,
        floatingActionButton = {
            ReadingPlanFabsComponent(
                uiState = uiState,
                onEvent = onEvent,
            )
        },
    ) {
        ReadingPlanScreen(
            uiState = uiState,
            onEvent = onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            lazyListState = lazyListState,
        )
    }
}

@Composable
private fun ReadingPlanScreenObserver(
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
    uiActionFlow: Flow<ReadingPlanUiAction>,
    uiState: ReadingPlanUiState,
    onNavigate: (NavKey) -> Unit,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val scrollToTop = uiState.scrollToTop
    val loadedUiState = remember(uiState) { uiState as? ReadingPlanUiState.Loaded }
    val activeWeekNumber = loadedUiState?.planStatus?.nextDay?.weekNumber
    LaunchedEffect(
        lazyListState.firstVisibleItemIndex,
        lazyListState.firstVisibleItemScrollOffset,
        activeWeekNumber,
    ) {
        val isScrolled = lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        onEvent(ReadingPlanUiEvent.OnScrollStateChange(isScrolled))
        val visibleKeys = lazyListState.layoutInfo.visibleItemsInfo.map { it.key }
        val isActiveRowVisible = visibleKeys.contains(HERO_ITEM_KEY) ||
            (activeWeekNumber != null && visibleKeys.contains(activeWeekNumber))
        onEvent(ReadingPlanUiEvent.OnActiveRowVisibilityChange(isActiveRowVisible))
    }

    ScrollToTopObserver(
        scrollToTop = scrollToTop,
        lazyListState = lazyListState,
        onEvent = onEvent,
    )

    ReadingPlanUiActionCollector(
        snackbarHostState = snackbarHostState,
        flow = uiActionFlow,
        onNavigate = onNavigate,
    )
}

private const val HERO_ITEM_KEY = "hero"
