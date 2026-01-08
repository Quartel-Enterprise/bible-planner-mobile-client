package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.component.BookList
import com.quare.bibleplanner.feature.books.presentation.component.BooksTopBar
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiAction
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalMainPadding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BooksScreen(
    state: BooksUiState,
    uiAction: Flow<BooksUiAction>,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val mainPadding = LocalMainPadding.current
    val searchGridState = rememberLazyGridState()
    val oldTestamentGridState = rememberLazyGridState()
    val newTestamentGridState = rememberLazyGridState()

    val isScrolled by remember(state) {
        derivedStateOf {
            val activeState = when {
                state !is BooksUiState.Success -> searchGridState
                !state.shouldShowTestamentToggle -> searchGridState
                state.selectedTestament == BookTestament.OldTestament -> oldTestamentGridState
                else -> newTestamentGridState
            }
            activeState.firstVisibleItemIndex > 0 || activeState.firstVisibleItemScrollOffset > 0
        }
    }

    ActionCollector(uiAction) { action ->
        when (action) {
            is BooksUiAction.ScrollToTop -> {
                searchGridState.animateScrollToItem(0)
                oldTestamentGridState.animateScrollToItem(0)
                newTestamentGridState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        topBar = {
            BooksTopBar(
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
                    BookList(
                        state = state,
                        onEvent = onEvent,
                        lazyGridState = searchGridState,
                        oldTestamentGridState = oldTestamentGridState,
                        newTestamentGridState = newTestamentGridState,
                    )
                }
            }
        }
    }
}
