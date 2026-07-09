package com.quare.bibleplanner.feature.logout.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.feature.logout.presentation.utils.LogoutUiActionCollector
import com.quare.bibleplanner.feature.logout.presentation.viewmodel.LogoutViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.logout(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<LogoutNavRoute>(
        metadata = DialogSceneStrategy.dialog(
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ),
    ) {
        LogoutRootContent(
            onNavigateBack = onNavigateBack,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun LogoutRootContent(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = koinViewModel<LogoutViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    LogoutUiActionCollector(
        uiActionFlow = viewModel.uiAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
    LogoutDialog(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
