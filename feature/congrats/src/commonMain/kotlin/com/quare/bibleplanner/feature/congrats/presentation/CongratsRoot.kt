package com.quare.bibleplanner.feature.congrats.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.CongratsNavRoute

fun NavGraphBuilder.congrats(navController: NavController) {
    composable<CongratsNavRoute> {
        CongratsScreen(
            onNavigateBack = { navController.navigateUp() },
        )
    }
}
