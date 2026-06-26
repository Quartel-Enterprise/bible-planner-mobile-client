package com.quare.bibleplanner.feature.main.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.navhost.BottomNavHost
import com.quare.bibleplanner.feature.main.presentation.screen.MainScreen
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.feature.notificationpermission.presentation.NotificationPermissionStartEffect
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.mainScreen(
    rootNavController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<MainNavRoute> {
        val bottomNavController: NavHostController = rememberNavController()
        val mainViewModel: MainScreenViewModel = koinViewModel()
        NotificationPermissionStartEffect(navController = rootNavController)
        ActionCollector(mainViewModel.uiAction) { uiAction ->
            when (uiAction) {
                is MainScreenUiAction.NavigateToBottomRoute -> {
                    bottomNavController.goToBottomNavRoute(uiAction.route)
                }
            }
        }
        MainScreen(
            mainViewModel = mainViewModel,
            bottomNavController = bottomNavController,
            bottomNavHost = { navigationBar, navigationRail ->
                BottomNavHost(
                    bottomNavController = bottomNavController,
                    rootNavController = rootNavController,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = this@composable,
                    navigationBar = navigationBar,
                    navigationRail = navigationRail,
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
