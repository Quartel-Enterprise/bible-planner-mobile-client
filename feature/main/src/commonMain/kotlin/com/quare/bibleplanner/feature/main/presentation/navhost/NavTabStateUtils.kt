package com.quare.bibleplanner.feature.main.presentation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.model.route.navigationSavedStateConfiguration

@Composable
internal fun rememberNavTabState(): NavTabState {
    val tabs: List<MainNavRouteDestination> = listOf(
        MainNavRouteDestination.Plans,
        MainNavRouteDestination.Books,
        MainNavRouteDestination.Profile,
    )
    val backStacks: Map<MainNavRouteDestination, NavBackStack<NavKey>> = tabs.associateWith { tab ->
        rememberNavBackStack(navigationSavedStateConfiguration, tab)
    }
    val selectedIndexState = rememberSaveable { mutableIntStateOf(0) }
    return remember {
        NavTabState(
            tabs = tabs,
            backStacks = backStacks,
            selectedIndexState = selectedIndexState,
        )
    }
}
