package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.books.presentation.component.BookList
import com.quare.bibleplanner.feature.books.presentation.component.BooksTopBar
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.utils.LocalMainPadding

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BooksScreen(
    state: BooksUiState,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val mainPadding = LocalMainPadding.current
    val lazyGridState = rememberLazyGridState()

    val isScrolled by remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex > 0 || lazyGridState.firstVisibleItemScrollOffset > 0
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
                    // Loading indicator
                }

                is BooksUiState.Success -> {
                    BookList(
                        state = state,
                        onEvent = onEvent,
                        lazyGridState = lazyGridState,
                    )
                }
            }
        }
    }
}
