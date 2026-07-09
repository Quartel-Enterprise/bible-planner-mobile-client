package com.quare.bibleplanner.feature.deleteprogress.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.feature.deleteprogress.presentation.utils.DeleteProgressUiActionCollector
import com.quare.bibleplanner.feature.deleteprogress.presentation.viewmodel.DeleteAllProgressViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.deleteProgress(onNavigateBack: () -> Unit) {
    entry<DeleteAllProgressNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel = koinViewModel<DeleteAllProgressViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        DeleteProgressUiActionCollector(
            uiActionFlow = viewModel.backUiAction,
            onNavigateBack = onNavigateBack,
        )
        DeleteAllProgressScreen(
            onEvent = viewModel::onEvent,
        )
    }
}
