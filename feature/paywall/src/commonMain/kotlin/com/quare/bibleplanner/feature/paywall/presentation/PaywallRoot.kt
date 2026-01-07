package com.quare.bibleplanner.feature.paywall.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.feature.paywall.presentation.utils.PaywallUiActionCollector
import com.quare.bibleplanner.feature.paywall.presentation.viewmodel.PaywallViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.paywall(
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
) {
    dialog<PaywallNavRoute> {
        val viewModel = koinViewModel<PaywallViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        PaywallUiActionCollector(
            actionsFlow = viewModel.uiAction,
            navController = navController,
            snackbarHostState = snackbarHostState,
        )
        PaywallBottomSheet(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
