package com.quare.bibleplanner.feature.deleteversion.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.feature.deleteversion.presentation.viewmodel.DeleteVersionViewModel
import com.quare.bibleplanner.ui.component.dialog.toNativeAlertDialogProperties
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.deleteVersion() {
    entry<DeleteVersionNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties().toNativeAlertDialogProperties()),
    ) { route ->
        val viewModel: DeleteVersionViewModel = koinViewModel { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()

        DeleteVersionScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
