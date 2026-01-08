package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun BookList(
    state: BooksUiState.Success,
    lazyGridState: LazyGridState,
    onEvent: (BooksUiEvent) -> Unit,
) {
    SharedTransitionLayout {
        val sharedTransitionScope = this
        AnimatedContent(
            targetState = state.layoutFormat,
            label = "layout_transition",
            transitionSpec = {
                EnterTransition.None togetherWith ExitTransition.None
            },
        ) { targetLayoutFormat ->
            val animatedVisibilityScope = this
            val isGrid = targetLayoutFormat == BookLayoutFormat.Grid
            val totalColumns = 2

            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Fixed(totalColumns),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(totalColumns) }) {
                    AnimatedVisibility(
                        visible = state.isInformationBoxVisible,
                        enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(),
                    ) {
                        BooksInformationBox(
                            onDismiss = { onEvent(BooksUiEvent.OnDismissInformationBox) },
                        )
                    }
                }

                if (state.shouldShowTestamentToggle) {
                    item(span = { GridItemSpan(totalColumns) }) {
                        BookTogglesToggles(
                            selectedTestament = state.selectedTestament,
                            selectedLayoutFormat = state.layoutFormat,
                            onEvent = onEvent,
                        )
                    }
                }

                if (!state.shouldShowTestamentToggle) {
                    items(
                        items = state.filteredBooks,
                        key = { it.id.name },
                        span = { GridItemSpan(if (isGrid) 1 else totalColumns) },
                    ) { book ->
                        BookItemComponent(
                            book = book,
                            layoutFormat = targetLayoutFormat,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onEvent = onEvent,
                        )
                    }
                } else {
                    state.groupsInTestament.forEach { presentationGroup ->
                        item(span = { GridItemSpan(totalColumns) }) {
                            BookGroupHeader(presentationGroup.group)
                        }

                        items(
                            items = presentationGroup.books,
                            key = { it.id.name },
                            span = { GridItemSpan(if (isGrid) 1 else totalColumns) },
                        ) { book ->
                            BookItemComponent(
                                book = book,
                                layoutFormat = targetLayoutFormat,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onEvent = onEvent,
                            )
                        }
                    }
                }
            }
        }
    }
}
