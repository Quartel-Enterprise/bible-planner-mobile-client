package com.quare.bibleplanner.feature.main.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.navhost.BottomNavHost
import com.quare.bibleplanner.feature.main.presentation.screen.MainScreen
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.MainScaffoldState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.mainScreen(
    mainScaffoldState: MainScaffoldState,
    rootNavController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<MainNavRoute> {
        val bottomNavController: NavHostController = rememberNavController()
        val bottomNavBackStackEntry by bottomNavController.currentBackStackEntryAsState()
        val mainViewModel: MainScreenViewModel = koinViewModel()
        ActionCollector(mainViewModel.uiAction) { uiAction ->
            when (uiAction) {
                is MainScreenUiAction.NavigateToBottomRoute -> bottomNavController.goToBottomNavRoute(uiAction.route)
                MainScreenUiAction.ClearFabs -> mainScaffoldState.clearFab()
            }
        }
        MainScreen(
            mainViewModel = mainViewModel,
            navBackStackEntry = bottomNavBackStackEntry,
            bottomNavHost = {
                BottomNavHost(
                    bottomNavController = bottomNavController,
                    rootNavController = rootNavController,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = this@composable,
                    mainScaffoldState = mainScaffoldState,
                )
            },
            mainScaffoldState = mainScaffoldState,
        )
    }
}

private fun NavHostController.goToBottomNavRoute(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
