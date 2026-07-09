package com.quare.bibleplanner.feature.bookdetails.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiAction
import com.quare.bibleplanner.feature.bookdetails.presentation.viewmodel.BookDetailsViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalSharedTransitionApi::class)
fun EntryProviderScope<NavKey>.bookDetails(
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
) {
    entry<BookDetailsNavRoute> { route ->
        val viewModel = koinViewModel<BookDetailsViewModel> { parametersOf(route) }
        val state by viewModel.uiState.collectAsState()
        ActionCollector(viewModel.uiAction) { uiAction ->
            when (uiAction) {
                BookDetailsUiAction.NavigateBack -> onNavigateBack()
                is BookDetailsUiAction.NavigateToRoute -> onNavigate(uiAction.route)
            }
        }
        BookDetailsScreen(
            platform = viewModel.platform,
            state = state,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
            onEvent = viewModel::onEvent,
        )
    }
}
