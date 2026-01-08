package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
    AnimatedContent(
        targetState = layoutFormat,
        label = "book_item_layout_transition",
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        modifier = modifier,
    ) { targetFormat ->
        if (targetFormat == BookLayoutFormat.List) {
            BookListItemComponent(
                book = book,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onEvent = onEvent,
            )
        } else {
            BookGridItemComponent(
                book = book,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onEvent = onEvent,
            )
        }
    }
}
