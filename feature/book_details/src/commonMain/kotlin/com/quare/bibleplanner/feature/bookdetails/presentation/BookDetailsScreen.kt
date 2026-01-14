package com.quare.bibleplanner.feature.bookdetails.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookDetailsScreen(
    state: BookDetailsUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BookDetailsUiEvent) -> Unit,
) {
    val scrollState = rememberScrollState()
    val isScrolled by remember {
        derivedStateOf { scrollState.value > 0 }
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp),
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
                            synopsis = state.synopsis,
                            bookCategoryName = state.bookCategoryName,
                            bookGroup = state.bookGroup,
                            isExpanded = state.isSynopsisExpanded,
                            onToggleExpanded = { onEvent(BookDetailsUiEvent.OnToggleSynopsisExpanded) },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        VerticalSpacer(24)
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
        }
    }
}
