package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.feature.themeselection.presentation.utils.ThemeSettingsUiActionCollector
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.themeSettings(navController: NavHostController) {
    dialog<ThemeNavRoute> {
        val viewModel = koinViewModel<ThemeSelectionViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        ThemeSettingsUiActionCollector(
            actionsFlow = viewModel.uiAction,
            navController = navController,
        )
        val onEvent = viewModel::onEvent

        ModalBottomSheet(onDismissRequest = { onEvent(ThemeSelectionUiEvent.OnDismiss) }) {
            ThemeSelectionContent(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}
