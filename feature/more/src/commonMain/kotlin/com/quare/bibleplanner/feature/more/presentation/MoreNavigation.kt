package com.quare.bibleplanner.feature.more.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.BottomNavRoute

fun NavGraphBuilder.more(navController: NavController) {
    composable<BottomNavRoute.More> {
        MoreScreen()
    }
}
