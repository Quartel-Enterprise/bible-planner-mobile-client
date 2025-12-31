package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.day.presentation.component.TimeEditionDialog
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.feature.day.presentation.util.DayUiActionCollector
import com.quare.bibleplanner.feature.day.presentation.viewmodel.DayViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.day(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<DayNavRoute> {
        val viewModel = koinViewModel<DayViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent
        val snackbarHostState = remember { SnackbarHostState() }

        DayUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            snackbarHostState = snackbarHostState,
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
            snackbarHostState = snackbarHostState,
            onEvent = viewModel::onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = this,
        )
    }
}
