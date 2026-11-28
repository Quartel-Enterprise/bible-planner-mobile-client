package com.quare.bibleplanner.android.feature.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationForwardHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.quare.bibleplanner.android.navigation.LocalMainAnimatedContentScope
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.navigation.withNativeEntries
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackDestination
import com.quare.bibleplanner.feature.main.presentation.LocalMainTabEntries
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.navhost.NavTabState
import com.quare.bibleplanner.feature.main.presentation.navhost.rememberNavTabState
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.transition.rememberNavigationTransitions
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun MainNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(key = this) {
    MainEntryContent(animatedContentScope = LocalNavAnimatedContentScope.current)
}

@Composable
private fun MainEntryContent(animatedContentScope: AnimatedContentScope) {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val tabState = rememberNavTabState()
    val trackDestination = koinInject<TrackDestination>()
    val selectedTab = tabState.selectedTab
    LaunchedEffect(selectedTab) {
        trackDestination(selectedTab)
    }
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is MainScreenUiAction.NavigateToBottomRoute ->
                (uiAction.route as? MainNavRouteDestination)?.let(tabState::switchTo)
        }
    }
    NavigationForwardHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isForwardEnabled = tabState.canGoForward,
        onForwardCompleted = tabState::goForward,
    )
    val tabItems by viewModel.mainNavigationItemModels.collectAsState()
    MainContent(
        tabItems = tabItems,
        selectedTab = selectedTab,
        onEvent = viewModel::onEvent,
    ) {
        MainTabsNavDisplay(
            tabState = tabState,
            animatedContentScope = animatedContentScope,
        )
    }
}

@Composable
private fun MainTabsNavDisplay(
    tabState: NavTabState,
    animatedContentScope: AnimatedContentScope,
) {
    val sharedTabEntries = checkNotNull(LocalMainTabEntries.current) {
        "RootAppNavDisplay provides the shared main tabs"
    }
    val sharedEntries: (NavKey) -> NavEntry<NavKey> = remember(sharedTabEntries, animatedContentScope) {
        entryProvider {
            // Why: MainContent draws the bar or rail, and LocalHostedTabBarHeight makes the shared tabs skip theirs.
            sharedTabEntries.register(
                scope = this,
                navigationBar = {},
                navigationRail = {},
                animatedContentScope = animatedContentScope,
            )
        }
    }
    val navigationTransitions = rememberNavigationTransitions()
    CompositionLocalProvider(LocalMainAnimatedContentScope provides animatedContentScope) {
        NavDisplay(
            entries = tabState.toDecoratedEntries(withNativeEntries(sharedEntries)),
            modifier = Modifier.fillMaxSize(),
            onBack = tabState::goBack,
            transitionSpec = { navigationTransitions.createTabSwitchTransition(scope = this) },
            popTransitionSpec = { navigationTransitions.createTabSwitchTransition(scope = this) },
            predictivePopTransitionSpec = { navigationTransitions.createTabSwitchTransition(scope = this) },
        )
    }
}
