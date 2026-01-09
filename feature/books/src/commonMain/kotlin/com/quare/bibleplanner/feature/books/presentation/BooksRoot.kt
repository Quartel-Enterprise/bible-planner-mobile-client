package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.open_site
import bibleplanner.feature.books.generated.resources.reading_not_available_yet
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiAction
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.feature.books.presentation.utils.BooksUiActionCollector
import com.quare.bibleplanner.feature.books.presentation.viewmodel.BooksViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.MainScaffoldState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.booksScreen(mainScaffoldState: MainScaffoldState) {
    composable<BottomNavRoute.Books> {
        val viewModel = koinViewModel<BooksViewModel>()
        val state by viewModel.uiState.collectAsState()

        val searchGridState = rememberLazyGridState()
        val searchListState = rememberLazyGridState()
        val oldTestamentGridState = rememberLazyGridState()
        val oldTestamentListState = rememberLazyGridState()
        val newTestamentGridState = rememberLazyGridState()
        val newTestamentListState = rememberLazyGridState()

        val isScrolled by remember(state) {
            derivedStateOf {
                calculateIsScrolled(
                    state = state,
                    searchGridState = searchGridState,
                    searchListState = searchListState,
                    oldTestamentGridState = oldTestamentGridState,
                    oldTestamentListState = oldTestamentListState,
                    newTestamentGridState = newTestamentGridState,
                    newTestamentListState = newTestamentListState,
                )
            }
        }
        val snackbarHostState = mainScaffoldState.snackbarHostState

        val uriHandler = LocalUriHandler.current
        BooksUiActionCollector(
            uiAction = viewModel.uiAction,
            searchGridState = searchGridState,
            searchListState = searchListState,
            oldTestamentGridState = oldTestamentGridState,
            oldTestamentListState = oldTestamentListState,
            newTestamentGridState = newTestamentGridState,
            newTestamentListState = newTestamentListState,
            uriHandler = uriHandler,
            snackbarHostState = snackbarHostState,
        )

        BooksScreen(
            state = state,
            onEvent = viewModel::onEvent,
            searchGridState = searchGridState,
            searchListState = searchListState,
            oldTestamentGridState = oldTestamentGridState,
            oldTestamentListState = oldTestamentListState,
            newTestamentGridState = newTestamentGridState,
            newTestamentListState = newTestamentListState,
            isScrolled = isScrolled,
        )
    }
}

private fun calculateIsScrolled(
    state: BooksUiState,
    searchGridState: LazyGridState,
    searchListState: LazyGridState,
    oldTestamentGridState: LazyGridState,
    oldTestamentListState: LazyGridState,
    newTestamentGridState: LazyGridState,
    newTestamentListState: LazyGridState,
): Boolean {
    val activeState = when {
        state !is BooksUiState.Success -> {
            searchGridState
        }

        !state.shouldShowTestamentToggle -> {
            if (state.layoutFormat == BookLayoutFormat.Grid) searchGridState else searchListState
        }

        state.selectedTestament == BookTestament.OldTestament -> {
            if (state.layoutFormat == BookLayoutFormat.Grid) oldTestamentGridState else oldTestamentListState
        }

        else -> {
            if (state.layoutFormat == BookLayoutFormat.Grid) newTestamentGridState else newTestamentListState
        }
    }
    return activeState.firstVisibleItemIndex > 0 || activeState.firstVisibleItemScrollOffset > 0
}
