package com.quare.bibleplanner.feature.deleteversion.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.feature.deleteversion.presentation.viewmodel.DeleteVersionViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.deleteVersion(onNavigateBack: () -> Unit) {
    entry<DeleteVersionNavRoute>(metadata = DialogSceneStrategy.dialog()) { route ->
        val viewModel: DeleteVersionViewModel = koinViewModel { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()

        ActionCollector(viewModel.backUiAction) {
            onNavigateBack()
        }

        DeleteVersionScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
