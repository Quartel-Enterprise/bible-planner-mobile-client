package com.quare.bibleplanner.feature.deleteprogress.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiAction
import com.quare.bibleplanner.feature.deleteprogress.presentation.viewmodel.DeleteAllProgressViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.deleteProgress(navController: NavHostController) {
    dialog<DeleteAllProgressNavRoute> {
        val viewModel = koinViewModel<DeleteAllProgressViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        ActionCollector(viewModel.uiAction) { uiAction ->
            when (uiAction) {
                DeleteAllProgressUiAction.NavigateBack -> {
                    navController.navigateUp()
                }
            }
        }
        DeleteAllProgressScreen(
            onEvent = viewModel::onEvent,
        )
    }
}
