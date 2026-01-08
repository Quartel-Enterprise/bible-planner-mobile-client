package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.feature.books.presentation.util.NoClip

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun BookList(
    state: BooksUiState.Success,
    searchGridState: LazyGridState,
    searchListState: LazyGridState,
    oldTestamentGridState: LazyGridState,
    oldTestamentListState: LazyGridState,
    newTestamentGridState: LazyGridState,
    newTestamentListState: LazyGridState,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val isGrid = state.layoutFormat == BookLayoutFormat.Grid
    val totalColumns = 2

    val currentLazyGridState = when {
        !state.shouldShowTestamentToggle -> if (isGrid) searchGridState else searchListState
        state.selectedTestament == BookTestament.OldTestament -> if (isGrid) oldTestamentGridState else oldTestamentListState
        else -> if (isGrid) newTestamentGridState else newTestamentListState
    }

    LazyVerticalGrid(
        state = currentLazyGridState,
        columns = GridCells.Fixed(totalColumns),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(
            span = { GridItemSpan(totalColumns) },
            key = "info_box_item",
        ) {
            AnimatedVisibility(
                visible = state.isInformationBoxVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                BooksInformationBox(
                    onDismiss = { onEvent(BooksUiEvent.OnDismissInformationBox) },
                )
            }
        }

        if (state.shouldShowTestamentToggle) {
            item(
                span = { GridItemSpan(totalColumns) },
                key = "testament_toggle_item",
            ) {
                BookToggles(
                    selectedTestament = state.selectedTestament,
                    selectedLayoutFormat = state.layoutFormat,
                    onEvent = onEvent,
                )
            }
        }

        if (!state.shouldShowTestamentToggle) {
            items(
                items = state.filteredBooks,
                key = { "book-${it.id.name}" },
                span = { GridItemSpan(if (isGrid) 1 else totalColumns) },
            ) { book ->
                BookItemComponent(
                    book = book,
                    layoutFormat = state.layoutFormat,
                    onEvent = onEvent,
                )
            }
        } else {
            state.groupsInTestament.forEach { presentationGroup ->
                item(
                    span = { GridItemSpan(totalColumns) },
                    key = "header-${presentationGroup.group}",
                ) {
                    BookGroupHeader(
                        group = presentationGroup.group,
                    )
                }

                items(
                    items = presentationGroup.books,
                    key = { "book-${it.id.name}" },
                    span = { GridItemSpan(if (isGrid) 1 else totalColumns) },
                ) { book ->
                    BookItemComponent(
                        book = book,
                        layoutFormat = state.layoutFormat,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}
