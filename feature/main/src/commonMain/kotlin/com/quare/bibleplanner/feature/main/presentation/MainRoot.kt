package com.quare.bibleplanner.feature.main.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationForwardHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackDestination
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.navhost.NavTabState
import com.quare.bibleplanner.feature.main.presentation.navhost.rememberNavTabState
import com.quare.bibleplanner.feature.main.presentation.screen.MainNavigationBar
import com.quare.bibleplanner.feature.main.presentation.screen.MainNavigationRail
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.isNativeNavigationBar
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val TAB_TRANSITION_DURATION_MILLIS = 300

fun EntryProviderScope<NavKey>.mainScreen(tabEntries: MainTabEntries) {
    entry<MainNavRoute> {
        MainRootContent(
            tabEntries = tabEntries,
            animatedContentScope = LocalNavAnimatedContentScope.current,
        )
    }
}

@Composable
private fun MainRootContent(
    tabEntries: MainTabEntries,
    animatedContentScope: AnimatedContentScope,
) {
    val mainViewModel: MainScreenViewModel = koinViewModel()
    val tabState: NavTabState = rememberNavTabState()
    val trackDestination = koinInject<TrackDestination>()
    val selectedTab = tabState.selectedTab
    LaunchedEffect(selectedTab) {
        trackDestination(selectedTab)
    }
    ActionCollector(mainViewModel.uiAction) { uiAction ->
        when (uiAction) {
            is MainScreenUiAction.NavigateToBottomRoute ->
                (uiAction.route as? MainNavRouteDestination)?.let(tabState::switchTo)
        }
    }
    val language by mainViewModel.languageState.collectAsState()
    val mainNavigationModels by mainViewModel.mainNavigationItemModels.collectAsState()
    val onEvent = mainViewModel::onEvent
    NavigationForwardHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isForwardEnabled = tabState.canGoForward,
        onForwardCompleted = tabState::goForward,
    )
    NavDisplay(
        entries = tabState.toDecoratedEntries(
            entryProvider {
                tabEntries.register(
                    scope = this,
                    navigationBar = { modifier ->
                        MainNavigationBar(
                            modifier = modifier,
                            isNativeBarVisible = !isNativeNavigationBar || animatedContentScope.transition.isSettled(),
                            selectedRoute = tabState.selectedTab,
                            mainNavigationModels = mainNavigationModels,
                            language = language,
                            onEvent = onEvent,
                        )
                    },
                    navigationRail = {
                        MainNavigationRail(
                            selectedRoute = tabState.selectedTab,
                            mainNavigationModels = mainNavigationModels,
                            language = language,
                            onEvent = onEvent,
                        )
                    },
                    animatedContentScope = animatedContentScope,
                )
            },
        ),
        modifier = Modifier.fillMaxSize(),
        onBack = tabState::goBack,
        transitionSpec = { createTabTransitionSpec() },
        popTransitionSpec = { createTabTransitionSpec() },
        predictivePopTransitionSpec = { createTabTransitionSpec() },
    )
}

private fun createTabTransitionSpec(): ContentTransform =
    fadeIn(animationSpec = tween(TAB_TRANSITION_DURATION_MILLIS)) togetherWith
        fadeOut(animationSpec = tween(TAB_TRANSITION_DURATION_MILLIS))

private fun Transition<EnterExitState>.isSettled(): Boolean =
    currentState == EnterExitState.Visible && targetState == EnterExitState.Visible
