package com.quare.bibleplanner.feature.books.presentation.utils

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler
import androidx.navigation.NavController
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.open_site
import bibleplanner.feature.books.generated.resources.reading_not_available_yet
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun BooksUiActionCollector(
    uiAction: Flow<BooksUiAction>,
    searchGridState: LazyGridState,
    searchListState: LazyGridState,
    oldTestamentGridState: LazyGridState,
    oldTestamentListState: LazyGridState,
    newTestamentGridState: LazyGridState,
    newTestamentListState: LazyGridState,
    uriHandler: UriHandler,
    snackbarHostState: SnackbarHostState,
    navController: NavController,
) {
    ActionCollector(uiAction) { action ->
        when (action) {
            is BooksUiAction.ScrollToTop -> {
                sequenceOf(
                    searchGridState,
                    searchListState,
                    oldTestamentGridState,
                    oldTestamentListState,
                    newTestamentGridState,
                    newTestamentListState,
                ).forEach { it.srollToTop() }
            }

            is BooksUiAction.OpenWebAppLink -> {
                uriHandler.openUri(action.url)
            }

            is BooksUiAction.ShowReadingNotAvailableYetSnackbar -> {
                val result = snackbarHostState.showSnackbar(
                    message = getString(Res.string.reading_not_available_yet),
                    actionLabel = getString(Res.string.open_site),
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    uriHandler.openUri(action.url)
                }
            }

            is BooksUiAction.NavigateToBookDetails -> {
                navController.navigate(BookDetailsNavRoute(action.bookId))
            }
        }
    }
}

private suspend fun LazyGridState.srollToTop() {
    animateScrollToItem(0)
}
