package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.ui.utils.MainScaffoldState
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    mainViewModel: MainScreenViewModel,
    navBackStackEntry: NavBackStackEntry?,
    mainScaffoldState: MainScaffoldState,
    bottomNavHost: @Composable (PaddingValues) -> Unit,
) {
    val onEvent = mainViewModel::dispatchUiEvent
    MainScreenContent(
        currentDestination = navBackStackEntry?.destination,
        bottomNavigationModels = mainViewModel.bottomNavigationItemModels,
        onEvent = onEvent,
        mainScaffoldState = mainScaffoldState,
        content = { paddingValues ->
            bottomNavHost(paddingValues)
        },
    )
}
