package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.feature.themeselection.presentation.utils.ThemeSettingsUiActionCollector
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.themeSettings(navController: NavHostController) {
    composable<ThemeNavRoute> {
        val viewModel = koinViewModel<ThemeSelectionViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        ThemeSettingsUiActionCollector(
            actionsFlow = viewModel.uiAction,
            navController = navController
        )
        ThemeSelectionScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
