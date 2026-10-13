package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import com.quare.bibleplanner.feature.books.presentation.component.BooksItemsComponent
import com.quare.bibleplanner.feature.books.presentation.component.BooksTopBar
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.feature.books.presentation.utils.BooksLazyGridStates
import com.quare.bibleplanner.ui.utils.LocalMainPadding

private const val BOOKS_CONTENT_TAG = "books_content"

@Composable
fun BooksScreen(
    state: BooksUiState,
    isScrolled: Boolean,
    lazyGridStates: BooksLazyGridStates,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val mainPadding = LocalMainPadding.current

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
        Box(
            modifier = Modifier
                .testTag(BOOKS_CONTENT_TAG)
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (state) {
                is BooksUiState.Loading -> AdaptiveCircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                is BooksUiState.Success -> {
                    BooksItemsComponent(
                        state = state,
                        onEvent = onEvent,
                        currentLazyGridState = lazyGridStates.getCurrent(state),
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
        }
    }
}
