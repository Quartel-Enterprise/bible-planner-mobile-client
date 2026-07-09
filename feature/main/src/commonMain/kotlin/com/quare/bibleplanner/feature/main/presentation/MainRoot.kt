package com.quare.bibleplanner.feature.main.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.quare.bibleplanner.feature.main.presentation.navhost.BottomNavTabState
import com.quare.bibleplanner.feature.main.presentation.navhost.rememberBottomNavTabState
import com.quare.bibleplanner.feature.main.presentation.screen.MainNavigationBar
import com.quare.bibleplanner.feature.main.presentation.screen.MainNavigationRail
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.feature.more.presentation.more
import com.quare.bibleplanner.feature.notificationpermission.presentation.NotificationPermissionStartEffect
import com.quare.bibleplanner.feature.readingplan.presentation.readingPlan
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

private const val TAB_TRANSITION_DURATION_MILLIS = 300

@OptIn(ExperimentalSharedTransitionApi::class)
fun EntryProviderScope<NavKey>.mainScreen(
    onNavigate: (Any) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
) {
    entry<MainNavRoute> {
        MainRootContent(
            onNavigate = onNavigate,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = LocalNavAnimatedContentScope.current,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MainRootContent(
    onNavigate: (Any) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val mainViewModel: MainScreenViewModel = koinViewModel()
    val tabState: BottomNavTabState = rememberBottomNavTabState()
    NotificationPermissionStartEffect(onNavigate)
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
        MainNavigationBar(
            modifier = modifier,
            selectedRoute = tabState.selectedTab,
            bottomNavigationModels = bottomNavigationModels,
            language = language,
            onEvent = onEvent,
        )
    }
    val navigationRail: @Composable () -> Unit = {
        MainNavigationRail(
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
        transitionSpec = { tabTransitionSpec() },
        popTransitionSpec = { tabTransitionSpec() },
        predictivePopTransitionSpec = { tabTransitionSpec() },
    )
}

private fun tabTransitionSpec(): ContentTransform =
    fadeIn(animationSpec = tween(TAB_TRANSITION_DURATION_MILLIS)) togetherWith
        fadeOut(animationSpec = tween(TAB_TRANSITION_DURATION_MILLIS))
