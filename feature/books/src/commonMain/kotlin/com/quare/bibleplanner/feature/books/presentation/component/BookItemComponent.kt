package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
    searchQuery: String,
    layoutFormat: BookLayoutFormat,
    onEvent: (BooksUiEvent) -> Unit,
) {
    SharedTransitionLayout {
        val sharedTransitionScope = this
        AnimatedContent(
            targetState = layoutFormat,
            label = "book_item_layout_transition_${book.id.name}",
        ) { targetLayoutFormat ->
            val animatedVisibilityScope = this
            if (targetLayoutFormat == BookLayoutFormat.List) {
                BookListItemComponent(
                    book = book,
                    searchQuery = searchQuery,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onEvent = onEvent,
                    modifier = modifier,
                )
            } else {
                BookGridItemComponent(
                    book = book,
                    searchQuery = searchQuery,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onEvent = onEvent,
                    modifier = modifier,
                )
            }
        }
    }
}
