package com.quare.bibleplanner.feature.unlockpremium.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.UnlockPremiumNavRoute
import com.quare.bibleplanner.feature.unlockpremium.presentation.utils.UnlockPremiumUiActionCollector
import com.quare.bibleplanner.feature.unlockpremium.presentation.viewmodel.UnlockPremiumViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.unlockPremium(navController: NavHostController) {
    composable<UnlockPremiumNavRoute> {
        val viewModel = koinViewModel<UnlockPremiumViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        UnlockPremiumUiActionCollector(
            actionsFlow = viewModel.uiAction,
            navController = navController,
        )
        UnlockPremiumScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
