package com.quare.bibleplanner.feature.deleteaccount.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.DeleteAccountNavRoute
import com.quare.bibleplanner.feature.deleteaccount.presentation.utils.DeleteAccountUiActionCollector
import com.quare.bibleplanner.feature.deleteaccount.presentation.viewmodel.DeleteAccountViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.deleteAccount(onNavigateBack: () -> Unit) {
    entry<DeleteAccountNavRoute>(
        metadata = DialogSceneStrategy.dialog(
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ),
    ) {
        val viewModel = koinViewModel<DeleteAccountViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        DeleteAccountUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigateBack = onNavigateBack,
        )
        DeleteAccountDialog(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
