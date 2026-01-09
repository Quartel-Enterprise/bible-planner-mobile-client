package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.component.ResponsiveGrid

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun BooksItemsComponent(
    state: BooksUiState.Success,
    currentLazyGridState: LazyGridState,
    onEvent: (BooksUiEvent) -> Unit,
) {
    BoxWithConstraints {
        val isGrid = state.layoutFormat == BookLayoutFormat.Grid
        val totalColumns = if (maxWidth > 600.dp) 3 else 2

        ResponsiveGrid(
            lazyGridState = currentLazyGridState,
            columns = GridCells.Fixed(totalColumns),
            maxContentWidth = 800.dp,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            portraitContent = {
                responsiveItem(
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
                            onEvent = onEvent,
                        )
                    }
                }

                if (state.shouldShowTestamentToggle) {
                    responsiveItem(
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
                    responsiveItems(
                        items = state.filteredBooks,
                        key = { "book-${it.id.name}" },
                        span = { GridItemSpan(if (isGrid) 1 else totalColumns) },
                    ) { book ->
                        BookItemComponent(
                            book = book,
                            searchQuery = state.searchQuery,
                            layoutFormat = state.layoutFormat,
                            onEvent = onEvent,
                        )
                    }
                } else {
                    state.groupsInTestament.forEach { presentationGroup ->
                        responsiveItem(
                            span = { GridItemSpan(totalColumns) },
                            key = "header-${presentationGroup.group}",
                        ) {
                            BookGroupHeader(
                                group = presentationGroup.group,
                            )
                        }

                        responsiveItems(
                            items = presentationGroup.books,
                            key = { "book-${it.id.name}" },
                            span = { GridItemSpan(if (isGrid) 1 else totalColumns) },
                        ) { book ->
                            BookItemComponent(
                                book = book,
                                searchQuery = state.searchQuery,
                                layoutFormat = state.layoutFormat,
                                onEvent = onEvent,
                            )
                        }
                    }
                }
            },
        )
    }
}
