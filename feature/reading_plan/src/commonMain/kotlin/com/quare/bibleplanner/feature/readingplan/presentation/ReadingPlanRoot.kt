package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.ReadingPlanNavRoute
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.readingPlan() {
    composable<ReadingPlanNavRoute> {
        val viewModel = koinViewModel<ReadingPlanViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        ReadingPlanScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
