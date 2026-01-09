package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.books.presentation.viewmodel.BooksViewModel
import com.quare.bibleplanner.ui.utils.MainScaffoldState
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.booksScreen(
    navController: NavController,
    mainScaffoldState: MainScaffoldState,
) {
    composable<BottomNavRoute.Books> {
        val viewModel = koinViewModel<BooksViewModel>()
        val state by viewModel.uiState.collectAsState()

        BooksScreen(
            state = state,
            uiAction = viewModel.uiAction,
            onEvent = viewModel::onEvent,
            snackbarHostState = mainScaffoldState.snackbarHostState,
        )
    }
}
