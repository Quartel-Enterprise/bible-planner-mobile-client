package com.quare.bibleplanner.feature.main.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.feature.main.presentation.navhost.BottomNavHost
import com.quare.bibleplanner.feature.main.presentation.screen.MainScreen
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.mainScreen(
    rootNavController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<MainNavRoute> {
        val bottomNavController: NavHostController = rememberNavController()
        val bottomNavBackStackEntry by bottomNavController.currentBackStackEntryAsState()
        val mainViewModel: MainScreenViewModel = koinViewModel()
        MainScreen(
            mainViewModel = mainViewModel,
            navBackStackEntry = bottomNavBackStackEntry,
            goToBottomNavRoute = bottomNavController::goToBottomNavRoute,
            bottomNavHost = {
                BottomNavHost(
                    rootNavController = rootNavController,
                    bottomNavController = bottomNavController,
                    sharedTransitionScope = sharedTransitionScope,
                )
            },
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
