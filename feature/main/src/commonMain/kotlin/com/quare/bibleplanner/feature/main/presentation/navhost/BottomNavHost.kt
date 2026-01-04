package com.quare.bibleplanner.feature.main.presentation.navhost

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.more.presentation.more
import com.quare.bibleplanner.feature.readingplan.presentation.readingPlan

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BottomNavHost(
    sharedTransitionScope: SharedTransitionScope,
    rootNavController: NavHostController,
    bottomNavController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = bottomNavController,
        startDestination = BottomNavRoute.Plans,
    ) {
        readingPlan(rootNavController, sharedTransitionScope)
        more(rootNavController)
    }
}
