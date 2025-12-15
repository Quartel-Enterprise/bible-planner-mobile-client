package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.day.presentation.component.TimeEditionDialog
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.feature.day.presentation.util.DayUiActionCollector
import com.quare.bibleplanner.feature.day.presentation.viewmodel.DayViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.day(navController: NavController) {
    composable<DayNavRoute> {
        val viewModel = koinViewModel<DayViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent
        DayUiActionCollector(
            uiActionFlow = viewModel.backUiAction,
            navController = navController,
        )
        (uiState as? DayUiState.Loaded)?.run {
            datePickerUiState.visiblePicker?.let {
                TimeEditionDialog(
                    type = it,
                    onEvent = onEvent,
                    datePickerUiState = datePickerUiState,
                )
            }
        }
        DayScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
