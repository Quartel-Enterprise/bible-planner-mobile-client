package com.quare.bibleplanner.feature.bookdetails.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.bookdetails.presentation.component.BookDetailsTopBar
import com.quare.bibleplanner.feature.bookdetails.presentation.component.ChaptersGrid
import com.quare.bibleplanner.feature.bookdetails.presentation.component.ReadingProgressCard
import com.quare.bibleplanner.feature.bookdetails.presentation.component.SynopsisSection
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiEvent
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.ui.component.ResponsiveColumn
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookDetailsScreen(
    state: BookDetailsUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BookDetailsUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }

    Scaffold(
        topBar = {
            BookDetailsTopBar(
                state = state,
                isScrolled = isScrolled,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onEvent = onEvent,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (state) {
                BookDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is BookDetailsUiState.Success -> {
                    ResponsiveColumn(
                        lazyListState = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        portraitContent = {
                            responsiveItem(key = "top_spacer") { VerticalSpacer(16) }
                            responsiveItem(key = "progress_card") {
                                ReadingProgressCard(
                                    progress = state.progress,
                                    readChaptersCount = state.readChaptersCount,
                                    totalChaptersCount = state.totalChaptersCount,
                                    bookIdName = state.id.name,
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                            }
                            responsiveItem(key = "mid_spacer_1") { VerticalSpacer(24) }
                            responsiveItem(key = "synopsis") {
                                SynopsisSection(
                                    synopsis = stringResource(state.synopsisStringResource),
                                    bookCategoryName = state.bookCategoryName,
                                    bookGroup = state.bookGroup,
                                    isExpanded = state.isSynopsisExpanded,
                                    onToggleExpanded = { onEvent(BookDetailsUiEvent.OnToggleSynopsisExpanded) },
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                            }
                            responsiveItem(key = "mid_spacer_2") { VerticalSpacer(24) }
                            responsiveItem(key = "chapters") {
                                ChaptersGrid(
                                    chapters = state.chapters,
                                    areAllChaptersRead = state.areAllChaptersRead,
                                    onChapterClick = { onEvent(BookDetailsUiEvent.OnChapterClick(it)) },
                                    onToggleAllClick = { onEvent(BookDetailsUiEvent.OnToggleAllChapters) },
                                )
                            }
                            responsiveItem(key = "bottom_spacer") { VerticalSpacer(32) }
                        },
                        landscapeContent = {
                            responsiveItem(key = "landscape_layout") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        VerticalSpacer(16)
                                        ReadingProgressCard(
                                            progress = state.progress,
                                            readChaptersCount = state.readChaptersCount,
                                            totalChaptersCount = state.totalChaptersCount,
                                            bookIdName = state.id.name,
                                            sharedTransitionScope = sharedTransitionScope,
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        )
                                        VerticalSpacer(24)
                                        SynopsisSection(
                                            synopsis = stringResource(state.synopsisStringResource),
                                            bookCategoryName = state.bookCategoryName,
                                            bookGroup = state.bookGroup,
                                            isExpanded = state.isSynopsisExpanded,
                                            onToggleExpanded = { onEvent(BookDetailsUiEvent.OnToggleSynopsisExpanded) },
                                            sharedTransitionScope = sharedTransitionScope,
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        )
                                        VerticalSpacer(32)
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        VerticalSpacer(16)
                                        ChaptersGrid(
                                            chapters = state.chapters,
                                            areAllChaptersRead = state.areAllChaptersRead,
                                            onChapterClick = { onEvent(BookDetailsUiEvent.OnChapterClick(it)) },
                                            onToggleAllClick = { onEvent(BookDetailsUiEvent.OnToggleAllChapters) },
                                        )
                                        VerticalSpacer(32)
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
