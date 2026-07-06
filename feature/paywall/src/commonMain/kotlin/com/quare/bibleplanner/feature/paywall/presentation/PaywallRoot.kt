package com.quare.bibleplanner.feature.paywall.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.feature.paywall.presentation.utils.PaywallUiActionCollector
import com.quare.bibleplanner.feature.paywall.presentation.viewmodel.PaywallViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.paywall(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<PaywallNavRoute> {
        val viewModel = koinViewModel<PaywallViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        PaywallUiActionCollector(
            actionsFlow = viewModel.uiAction,
            navController = navController,
            snackbarHostState = snackbarHostState,
        )

        PaywallScreen(
            platform = viewModel.platform,
            snackbarHostState = snackbarHostState,
            uiState = uiState,
            onEvent = viewModel::onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this@composable,
        )
    }
}
