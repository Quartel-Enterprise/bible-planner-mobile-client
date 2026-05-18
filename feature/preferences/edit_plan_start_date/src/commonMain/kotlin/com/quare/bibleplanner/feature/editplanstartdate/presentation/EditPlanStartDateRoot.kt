package com.quare.bibleplanner.feature.editplanstartdate.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.feature.editplanstartdate.presentation.component.EditPlanStartDateDialog
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiState
import com.quare.bibleplanner.feature.editplanstartdate.presentation.viewmodel.EditPlanStartDateViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.editPlanStartDate(navController: NavHostController) {
    dialog<EditPlanStartDateNavRoute> {
        val viewModel: EditPlanStartDateViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent

        ActionCollector(viewModel.dismissUiAction) {
            navController.navigateUp()
        }

        when (val state = uiState) {
            is EditPlanStartDateUiState.Loading -> {
                Unit
            }

            is EditPlanStartDateUiState.Loaded -> {
                EditPlanStartDateDialog(
                    initialTimestamp = state.initialTimestamp,
                    onEvent = onEvent,
                )
            }
        }
    }
}
