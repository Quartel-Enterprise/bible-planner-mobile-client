package com.quare.bibleplanner.feature.paywall.presentation

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

fun NavGraphBuilder.paywall(navController: NavHostController) {
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
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onEvent = viewModel::onEvent,
        )
    }
}
