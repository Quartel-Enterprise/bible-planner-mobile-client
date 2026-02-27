package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.feature.books.presentation.component.BooksItemsComponent
import com.quare.bibleplanner.feature.books.presentation.component.BooksTopBar
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.utils.LocalMainPadding

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BooksScreen(
    state: BooksUiState,
    isScrolled: Boolean,
    searchGridState: LazyGridState,
    searchListState: LazyGridState,
    oldTestamentGridState: LazyGridState,
    oldTestamentListState: LazyGridState,
    newTestamentGridState: LazyGridState,
    newTestamentListState: LazyGridState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val mainPadding = LocalMainPadding.current

    Scaffold(
        topBar = {
            BooksTopBar(
                modifier = Modifier.navigationBarsPadding(),
                state = state,
                onEvent = onEvent,
                contentPadding = mainPadding,
                isScrolled = isScrolled,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                is BooksUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is BooksUiState.Success -> {
                    val isGrid = state.layoutFormat == BookLayoutFormat.Grid
                    val currentLazyGridState = if (state.shouldShowTestamentToggle) {
                        when (state.selectedTestament) {
                            BookTestament.OldTestament -> if (isGrid) oldTestamentGridState else oldTestamentListState
                            BookTestament.NewTestament -> if (isGrid) newTestamentGridState else newTestamentListState
                        }
                    } else {
                        if (isGrid) searchGridState else searchListState
                    }

                    BooksItemsComponent(
                        state = state,
                        onEvent = onEvent,
                        currentLazyGridState = currentLazyGridState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
        }
    }
}
