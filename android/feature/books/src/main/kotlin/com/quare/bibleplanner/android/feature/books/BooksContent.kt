package com.quare.bibleplanner.android.feature.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState

// Why: the same breakpoint and width cap as the shared screen's ResponsiveGrid.
private val threeColumnsMinWidth = 600.dp
private val contentMaxWidth = 800.dp
private val listSidePadding = 16.dp
private val listBottomPadding = 16.dp
private const val BOOKS_CONTENT_TAG = "books_content"

@Composable
internal fun BooksContent(
    state: BooksUiState,
    isScrolled: Boolean,
    lazyGridState: LazyGridState,
    transitionScopes: BooksTransitionScopes?,
    hostedTabBarHeight: Dp,
    onEvent: (BooksUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            BooksTopBar(
                state = state,
                isScrolled = isScrolled,
                onEvent = onEvent,
            )
        },
        // Why: BooksTopBar pads for side display cutouts, so the body does too and the books stay aligned under it.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        // Why: the books scroll under the main screen's bar, so the last one clears it while it shows.
        val bottomPadding = innerPadding.calculateBottomPadding() + hostedTabBarHeight
        val bodyModifier = Modifier
            .padding(
                start = innerPadding.calculateStartPadding(layoutDirection),
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(layoutDirection),
            ).fillMaxSize()
        when (state) {
            BooksUiState.Loading -> Box(
                modifier = bodyModifier.padding(bottom = bottomPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is BooksUiState.Success -> BooksGrid(
                state = state,
                lazyGridState = lazyGridState,
                bottomPadding = bottomPadding,
                transitionScopes = transitionScopes,
                onEvent = onEvent,
                modifier = bodyModifier,
            )
        }
    }
}

@Composable
private fun BooksGrid(
    state: BooksUiState.Success,
    lazyGridState: LazyGridState,
    bottomPadding: Dp,
    transitionScopes: BooksTransitionScopes?,
    onEvent: (BooksUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val totalColumns = if (maxWidth > threeColumnsMinWidth) 3 else 2
        val centeringPadding = ((maxWidth - maxWidth.coerceAtMost(contentMaxWidth)) / 2).coerceAtLeast(0.dp)
        LazyVerticalGrid(
            columns = GridCells.Fixed(totalColumns),
            state = lazyGridState,
            // Why: the baseline profile journeys (tools/baseline-profile) find and fling the books list by this tag.
            modifier = Modifier
                .testTag(BOOKS_CONTENT_TAG)
                .fillMaxSize(),
            /*
             * Why: the bottom inset goes into the content padding instead of around the grid, so the books scroll
             * under the gesture bar edge to edge and the last one still clears it.
             */
            contentPadding = PaddingValues(
                start = listSidePadding + centeringPadding,
                end = listSidePadding + centeringPadding,
                bottom = listBottomPadding + bottomPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            booksGridItems(
                state = state,
                totalColumns = totalColumns,
                transitionScopes = transitionScopes,
                onEvent = onEvent,
            )
        }
    }
}

private fun LazyGridScope.booksGridItems(
    state: BooksUiState.Success,
    totalColumns: Int,
    transitionScopes: BooksTransitionScopes?,
    onEvent: (BooksUiEvent) -> Unit,
) {
    if (!state.shouldShowTestamentToggle) {
        bookItems(
            books = state.filteredBooks,
            state = state,
            totalColumns = totalColumns,
            transitionScopes = transitionScopes,
            onEvent = onEvent,
        )
        return
    }
    item(
        key = "testament_toggle_item",
        span = { GridItemSpan(totalColumns) },
    ) {
        BooksToggleRow(
            selectedTestamentFilter = state.selectedTestamentFilter,
            isTestamentMenuVisible = state.isTestamentMenuVisible,
            selectedLayoutFormat = state.layoutFormat,
            onEvent = onEvent,
        )
    }
    state.visibleGroups.forEach { presentationGroup ->
        item(
            key = "header-${presentationGroup.group}",
            span = { GridItemSpan(totalColumns) },
        ) {
            BookGroupHeader(
                group = presentationGroup.group,
                transitionScopes = transitionScopes,
            )
        }
        bookItems(
            books = presentationGroup.books,
            state = state,
            totalColumns = totalColumns,
            transitionScopes = transitionScopes,
            onEvent = onEvent,
        )
    }
}

private fun LazyGridScope.bookItems(
    books: List<BookPresentationModel>,
    state: BooksUiState.Success,
    totalColumns: Int,
    transitionScopes: BooksTransitionScopes?,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val isGrid = state.layoutFormat == BookLayoutFormat.Grid
    items(
        items = books,
        key = { book -> "book-${book.id.name}" },
        span = { GridItemSpan(if (isGrid) 1 else totalColumns) },
    ) { book ->
        when (state.layoutFormat) {
            BookLayoutFormat.List -> BookListItem(
                book = book,
                searchQuery = state.searchQuery,
                transitionScopes = transitionScopes,
                onEvent = onEvent,
            )

            BookLayoutFormat.Grid -> BookGridItem(
                book = book,
                searchQuery = state.searchQuery,
                transitionScopes = transitionScopes,
                onEvent = onEvent,
            )
        }
    }
}
