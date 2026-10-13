package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.feature.books.presentation.utils.BooksUiActionCollector
import com.quare.bibleplanner.feature.books.presentation.utils.rememberBooksLazyGridStates
import com.quare.bibleplanner.feature.books.presentation.viewmodel.BooksViewModel
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import com.quare.bibleplanner.ui.utils.MainTabScaffold
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.booksScreen(
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    entry<MainNavRouteDestination.Books> {
        BooksTabContent(
            navigationBar = navigationBar,
            navigationRail = navigationRail,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}

@Composable
private fun BooksTabContent(
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val viewModel = koinViewModel<BooksViewModel>()
    val state by viewModel.uiState.collectAsState()

    val lazyGridStates = rememberBooksLazyGridStates()

    val isScrolled by remember(state) {
        derivedStateOf {
            val activeState = lazyGridStates.getCurrent(state)
            activeState.firstVisibleItemIndex > 0 || activeState.firstVisibleItemScrollOffset > 0
        }
    }
    val snackbarHostState = LocalSnackbarHostState.current

    val uriHandler = LocalUriHandler.current
    BooksUiActionCollector(
        uiAction = viewModel.uiAction,
        lazyGridStates = lazyGridStates,
        uriHandler = uriHandler,
        snackbarHostState = snackbarHostState,
    )

    MainTabScaffold(
        navigationBar = navigationBar,
        navigationRail = navigationRail,
    ) {
        BooksScreen(
            state = state,
            onEvent = viewModel::onEvent,
            lazyGridStates = lazyGridStates,
            isScrolled = isScrolled,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}
