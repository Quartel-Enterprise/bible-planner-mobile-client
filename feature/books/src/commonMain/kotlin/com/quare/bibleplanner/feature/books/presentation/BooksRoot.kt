package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.feature.books.presentation.utils.BooksUiActionCollector
import com.quare.bibleplanner.feature.books.presentation.viewmodel.BooksViewModel
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import com.quare.bibleplanner.ui.utils.MainTabScaffold
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun EntryProviderScope<NavKey>.booksScreen(
    onNavigate: (Any) -> Unit,
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    entry<BottomNavRoute.Books> {
        BooksTabContent(
            onNavigate = onNavigate,
            navigationBar = navigationBar,
            navigationRail = navigationRail,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun BooksTabContent(
    onNavigate: (Any) -> Unit,
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
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
    val snackbarHostState = LocalSnackbarHostState.current

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
        onNavigate = onNavigate,
    )

    MainTabScaffold(
        navigationBar = navigationBar,
        navigationRail = navigationRail,
    ) {
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
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
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
