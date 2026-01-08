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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.util.NoClip
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState

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
    SharedTransitionLayout {
        val sharedTransitionScope = this

        AnimatedContent(
            targetState = Triple(
                state.selectedTestament,
                state.shouldShowTestamentToggle,
                state.layoutFormat
            ),
            label = "books_content_transition",
            transitionSpec = {
                val (initialTestament, initialShowToggle, initialLayout) = this.initialState
                val (targetTestament, targetShowToggle, targetLayout) = this.targetState

                val duration = 500
                val animationSpec = tween<IntOffset>(duration)
                val fadeSpec = tween<Float>(duration)
                val transform = when {
                    initialTestament != targetTestament && initialShowToggle && targetShowToggle -> {
                        val isMovingToNew = targetTestament == BookTestament.NewTestament
                        if (isMovingToNew) {
                            slideInHorizontally(animationSpec) { it } + fadeIn(fadeSpec) togetherWith
                                slideOutHorizontally(animationSpec) { -it } + fadeOut(fadeSpec)
                        } else {
                            slideInHorizontally(animationSpec) { -it } + fadeIn(fadeSpec) togetherWith
                                slideOutHorizontally(animationSpec) { it } + fadeOut(fadeSpec)
                        }
                    }
                    else -> {
                        fadeIn(fadeSpec) togetherWith fadeOut(fadeSpec)
                    }
                }

                transform.apply { targetContentZIndex = 1f }
            },
        ) { (targetTestament, showTestamentToggle, targetLayoutFormat) ->
            val animatedVisibilityScope = this
            val isGrid = targetLayoutFormat == BookLayoutFormat.Grid
            val totalColumns = 2

            val currentLazyGridState = when {
                !showTestamentToggle -> if (isGrid) searchGridState else searchListState
                targetTestament == BookTestament.OldTestament -> if (isGrid) oldTestamentGridState else oldTestamentListState
                else -> if (isGrid) newTestamentGridState else newTestamentListState
            }

            LazyVerticalGrid(
                state = currentLazyGridState,
                columns = GridCells.Fixed(totalColumns),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                            modifier = with(sharedTransitionScope) {
                                Modifier.sharedBounds(
                                    rememberSharedContentState(key = "info_box"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    clipInOverlayDuringTransition = NoClip,
                                )
                            },
                        )
                    }
                }

                if (showTestamentToggle) {
                    item(
                        span = { GridItemSpan(totalColumns) },
                        key = "testament_toggle_item",
                    ) {
                        BookToggles(
                            selectedTestament = targetTestament,
                            selectedLayoutFormat = targetLayoutFormat,
                            onEvent = onEvent,
                            modifier = with(sharedTransitionScope) {
                                Modifier.sharedBounds(
                                    rememberSharedContentState(key = "testament_toggles"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    clipInOverlayDuringTransition = NoClip,
                                )
                            },
                        )
                    }
                }

                if (!showTestamentToggle) {
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
