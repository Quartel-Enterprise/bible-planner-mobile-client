package com.quare.bibleplanner.feature.bookdetails.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiAction
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiEvent
import com.quare.bibleplanner.feature.bookdetails.presentation.viewmodel.BookDetailsViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.bookDetails(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<BookDetailsNavRoute> {
        val viewModel = koinViewModel<BookDetailsViewModel>()
        val state by viewModel.uiState.collectAsState()
        ActionCollector(viewModel.uiAction) { uiAction ->
            when (uiAction) {
                BookDetailsUiAction.NavigateBack -> navController.navigateUp()
                is BookDetailsUiAction.NavigateToRoute -> navController.navigate(uiAction.route)
            }
        }
        BookDetailsScreen(
            state = state,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this,
            onEvent = viewModel::onEvent,
        )
    }
}
