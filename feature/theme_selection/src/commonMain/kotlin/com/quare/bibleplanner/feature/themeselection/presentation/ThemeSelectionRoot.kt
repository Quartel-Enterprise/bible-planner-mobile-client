package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.themeSettings(navController: NavHostController) {
    composable<ThemeNavRoute> {
        val viewModel = koinViewModel<ThemeSelectionViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        ActionCollector(viewModel.uiAction) { uiAction ->
            when (uiAction) {
                ThemeSelectionUiAction.NavigateBack -> navController.navigateUp()
                ThemeSelectionUiAction.NavigateToMaterialYou -> navController.navigate(MaterialYouBottomSheetNavRoute)
            }
        }
        ThemeSelectionScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
