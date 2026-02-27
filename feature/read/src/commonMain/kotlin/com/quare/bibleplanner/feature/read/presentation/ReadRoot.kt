package com.quare.bibleplanner.feature.read.presentation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.feature.read.presentation.utils.ReadUiActionCollector
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.read(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<ReadNavRoute> {
        val viewModel: ReadViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsState()
        ReadUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navController,
        )
        ReadScreen(
            state = state,
            onEvent = viewModel::onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this,
        )
    }
}
