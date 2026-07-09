package com.quare.bibleplanner.feature.main.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.feature.books.presentation.booksScreen
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.navhost.Nav3BottomNavTabState
import com.quare.bibleplanner.feature.main.presentation.navhost.rememberNav3BottomNavTabState
import com.quare.bibleplanner.feature.main.presentation.screen.Nav3MainNavigationBar
import com.quare.bibleplanner.feature.main.presentation.screen.Nav3MainNavigationRail
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.feature.more.presentation.more
import com.quare.bibleplanner.feature.readingplan.presentation.readingPlan
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun EntryProviderScope<NavKey>.mainScreen(
    onNavigate: (Any) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
) {
    entry<MainNavRoute> {
        Nav3MainContent(
            onNavigate = onNavigate,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = LocalNavAnimatedContentScope.current,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Nav3MainContent(
    onNavigate: (Any) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val mainViewModel: MainScreenViewModel = koinViewModel()
    val tabState: Nav3BottomNavTabState = rememberNav3BottomNavTabState()
    ActionCollector(mainViewModel.uiAction) { uiAction ->
        when (uiAction) {
            is MainScreenUiAction.NavigateToBottomRoute -> {
                (uiAction.route as? BottomNavRoute)?.let(tabState::switchTo)
            }
        }
    }
    val language by mainViewModel.languageState.collectAsState()
    val bottomNavigationModels = mainViewModel.bottomNavigationItemModels
    val onEvent = mainViewModel::dispatchUiEvent
    val navigationBar: @Composable (Modifier) -> Unit = { modifier ->
        Nav3MainNavigationBar(
            modifier = modifier,
            selectedRoute = tabState.selectedTab,
            bottomNavigationModels = bottomNavigationModels,
            language = language,
            onEvent = onEvent,
        )
    }
    val navigationRail: @Composable () -> Unit = {
        Nav3MainNavigationRail(
            selectedRoute = tabState.selectedTab,
            bottomNavigationModels = bottomNavigationModels,
            language = language,
            onEvent = onEvent,
        )
    }
    NavDisplay(
        entries = tabState.toDecoratedEntries(
            entryProvider {
                readingPlan(
                    onNavigate = onNavigate,
                    navigationBar = navigationBar,
                    navigationRail = navigationRail,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
                booksScreen(
                    onNavigate = onNavigate,
                    navigationBar = navigationBar,
                    navigationRail = navigationRail,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedContentScope,
                )
                more(
                    onNavigate = onNavigate,
                    navigationBar = navigationBar,
                    navigationRail = navigationRail,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
            },
        ),
        modifier = Modifier.fillMaxSize(),
        onBack = { tabState.goBack() },
    )
}
