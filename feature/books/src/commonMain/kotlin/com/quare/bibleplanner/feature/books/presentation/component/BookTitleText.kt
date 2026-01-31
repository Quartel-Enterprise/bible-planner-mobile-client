package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.ui.utils.highlightText

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BookTitleText(
    book: BookPresentationModel,
    searchQuery: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    with(sharedTransitionScope) {
        Text(
            text = book.name.highlightText(
                query = searchQuery,
                highlightStyle = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    background = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
            ),
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier.sharedBounds(
                rememberSharedContentState(key = "title-${book.id.name}"),
                animatedVisibilityScope = animatedVisibilityScope,
            ),
        )
    }
}
