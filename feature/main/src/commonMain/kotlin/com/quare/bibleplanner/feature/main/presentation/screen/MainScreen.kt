package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    mainViewModel: MainScreenViewModel,
    navBackStackEntry: NavBackStackEntry?,
    goToBottomNavRoute: (Any) -> Unit,
    bottomNavHost: @Composable () -> Unit,
) {
    val onEvent = mainViewModel::dispatchUiEvent

    ActionCollector(mainViewModel.uiAction) { uiAction ->
        when (uiAction) {
            is MainScreenUiAction.NavigateToBottomRoute -> goToBottomNavRoute(uiAction.route)
        }
    }
    MainScreenContent(
        currentDestination = navBackStackEntry?.destination,
        bottomNavigationModels = mainViewModel.bottomNavigationItemModels,
        onEvent = onEvent,
        content = bottomNavHost,
    )
}
