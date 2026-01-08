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
    modifier: Modifier = Modifier,
    book: BookPresentationModel,
    layoutFormat: BookLayoutFormat,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BooksUiEvent) -> Unit,
) {
    if (layoutFormat == BookLayoutFormat.List) {
        BookListItemComponent(
            book = book,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onEvent = onEvent,
            modifier = modifier,
        )
    } else {
        BookGridItemComponent(
            book = book,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}
