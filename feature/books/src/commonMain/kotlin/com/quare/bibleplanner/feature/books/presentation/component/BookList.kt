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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
            targetState = state.selectedTestament to state.shouldShowTestamentToggle,
            label = "testament_transition",
            transitionSpec = {
                val (initialTestament, initialShowToggle) = initialState
                val (targetTestament, targetShowToggle) = targetState

                val duration = 500
                val animationSpec = tween<IntOffset>(duration)
                val fadeSpec = tween<Float>(duration)

                if (initialTestament != targetTestament) {
                    if (initialShowToggle && targetShowToggle) {
                        val isMovingToNew = targetTestament == BookTestament.NewTestament
                        if (isMovingToNew) {
                            (slideInHorizontally(animationSpec) { it } + fadeIn(fadeSpec)) togetherWith
                                (slideOutHorizontally(animationSpec) { -it } + fadeOut(fadeSpec))
                        } else {
                            (slideInHorizontally(animationSpec) { -it } + fadeIn(fadeSpec)) togetherWith
                                (slideOutHorizontally(animationSpec) { it } + fadeOut(fadeSpec))
                        }
                    } else {
                        fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
                    }
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
        ) { (targetTestament, showTestamentToggle) ->
            val testamentScope = this

            AnimatedContent(
                targetState = state.layoutFormat,
                label = "layout_transition",
                transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
            ) { targetLayoutFormat ->
                val layoutScope = this
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
                                        animatedVisibilityScope = testamentScope,
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
                                selectedLayoutFormat = state.layoutFormat,
                                onEvent = onEvent,
                                modifier = with(sharedTransitionScope) {
                                    Modifier.sharedBounds(
                                        rememberSharedContentState(key = "testament_toggles"),
                                        animatedVisibilityScope = testamentScope,
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
                                animatedVisibilityScope = layoutScope,
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
                                    modifier = Modifier.animateItem(
                                        placementSpec = tween(500),
                                    ),
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
                                    animatedVisibilityScope = layoutScope,
                                    onEvent = onEvent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
