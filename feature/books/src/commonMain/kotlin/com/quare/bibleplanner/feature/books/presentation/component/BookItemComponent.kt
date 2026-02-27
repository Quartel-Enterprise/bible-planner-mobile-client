package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookItemComponent(
    book: BookPresentationModel,
    searchQuery: String,
    layoutFormat: BookLayoutFormat,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BooksUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (layoutFormat) {
        BookLayoutFormat.List -> BookListItemComponent(
            modifier = modifier,
            book = book,
            searchQuery = searchQuery,
            screensSharedTransitionScope = sharedTransitionScope,
            screensAnimatedVisibilityScope = animatedVisibilityScope,
            onEvent = onEvent,
        )

        BookLayoutFormat.Grid -> BookGridItemComponent(
            modifier = modifier,
            book = book,
            searchQuery = searchQuery,
            screensSharedTransitionScope = sharedTransitionScope,
            screensAnimatedVisibilityScope = animatedVisibilityScope,
            onEvent = onEvent,
        )
    }
}
