package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel

@Composable
fun MainScreen(
    mainViewModel: MainScreenViewModel,
    bottomNavController: NavHostController,
    bottomNavHost: @Composable (
        navigationBar: @Composable (Modifier) -> Unit,
        navigationRail: @Composable () -> Unit,
    ) -> Unit,
) {
    val language by mainViewModel.languageState.collectAsState()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination: NavDestination? = navBackStackEntry?.destination
    val onEvent = mainViewModel::dispatchUiEvent
    val bottomNavigationModels = mainViewModel.bottomNavigationItemModels
    bottomNavHost(
        { modifier ->
            MainNavigationBar(
                modifier = modifier,
                currentDestination = currentDestination,
                bottomNavigationModels = bottomNavigationModels,
                language = language,
                onEvent = onEvent,
            )
        },
        {
            MainNavigationRail(
                currentDestination = currentDestination,
                bottomNavigationModels = bottomNavigationModels,
                language = language,
                onEvent = onEvent,
            )
        },
    )
}
