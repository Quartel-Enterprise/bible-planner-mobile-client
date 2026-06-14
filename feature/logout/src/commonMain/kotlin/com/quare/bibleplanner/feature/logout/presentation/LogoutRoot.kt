package com.quare.bibleplanner.feature.logout.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.feature.logout.presentation.utils.LogoutUiActionCollector
import com.quare.bibleplanner.feature.logout.presentation.viewmodel.LogoutViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.logout(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    dialog<LogoutNavRoute>(
        dialogProperties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        val viewModel = koinViewModel<LogoutViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        LogoutUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navController,
            snackbarHostState = snackbarHostState,
        )
        LogoutDialog(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
