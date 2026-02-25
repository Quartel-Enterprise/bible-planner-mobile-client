package com.quare.bibleplanner.feature.read.presentation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.read(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<ReadNavRoute> {
        val viewModel: ReadViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        ActionCollector(viewModel.uiAction) { uiAction ->
            when (uiAction) {
                ReadUiAction.NavigateBack -> navController.navigateUp()
                is ReadUiAction.NavigateToRoute -> navController.navigate(uiAction.route)
            }
        }
        ReadScreen(
            state = state,
            onEvent = viewModel::onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this,
        )
    }
}
