package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavBackStackEntry
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.ui.utils.LocalMainPadding
import com.quare.bibleplanner.ui.utils.MainScaffoldState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    mainViewModel: MainScreenViewModel,
    navBackStackEntry: NavBackStackEntry?,
    mainScaffoldState: MainScaffoldState,
    bottomNavHost: @Composable () -> Unit,
) {
    val onEvent = mainViewModel::dispatchUiEvent
    MainScreenContent(
        currentDestination = navBackStackEntry?.destination,
        bottomNavigationModels = mainViewModel.bottomNavigationItemModels,
        onEvent = onEvent,
        mainScaffoldState = mainScaffoldState,
        content = { paddingValues ->
            CompositionLocalProvider(
                value = LocalMainPadding provides paddingValues,
                content = bottomNavHost,
            )
        },
    )
}
