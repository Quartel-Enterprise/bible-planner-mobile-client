package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.ui.component.icon.FavoriteIconButton
import com.quare.bibleplanner.ui.component.progress.BookProgressBar
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BookListItemComponent(
    book: BookPresentationModel,
    searchQuery: String,
    screensSharedTransitionScope: SharedTransitionScope,
    screensAnimatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BooksUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bookIdName = book.id.name
    BookCard(
        modifier = modifier,
        book = book,
        onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
        sharedTransitionScope = screensSharedTransitionScope,
        animatedVisibilityScope = screensAnimatedVisibilityScope,
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookTypeIcon(
                modifier = modifier
                    .size(40.dp),
                book = book,
                sharedTransitionScope = screensSharedTransitionScope,
                animatedVisibilityScope = screensAnimatedVisibilityScope,
                iconSize = 20.dp,
                shape = RoundedCornerShape(8.dp),
                iconAlpha = 0.5f,
            )

            HorizontalSpacer(16.dp)

            Column(modifier = Modifier.weight(1f)) {
                BookTitleText(
                    book = book,
                    searchQuery = searchQuery,
                    sharedTransitionScope = screensSharedTransitionScope,
                    animatedVisibilityScope = screensAnimatedVisibilityScope,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BookProgressNumbers(
                        book = book,
                        sharedTransitionScope = screensSharedTransitionScope,
                        animatedVisibilityScope = screensAnimatedVisibilityScope,
                        style = MaterialTheme.typography.bodySmall,
                    )

                    HorizontalSpacer(8.dp)
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            ),
                    )
                    HorizontalSpacer(8.dp)
                    BookPercentageText(
                        progress = book.progress,
                        bookIdName = bookIdName,
                        sharedTransitionScope = screensSharedTransitionScope,
                        animatedVisibilityScope = screensAnimatedVisibilityScope,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                VerticalSpacer(8.dp)
                BookProgressBar(
                    progress = if (book.isCompleted) 1f else book.progress,
                    bookIdName = bookIdName,
                    sharedTransitionScope = screensSharedTransitionScope,
                    animatedVisibilityScope = screensAnimatedVisibilityScope,
                    modifier = Modifier
                        .fillMaxWidth(),
                    height = 4.dp,
                )
            }

            FavoriteIconButton(
                modifier = Modifier.size(40.dp),
                isFavorite = book.isFavorite,
                sharedTransitionScope = screensSharedTransitionScope,
                animatedVisibilityScope = screensAnimatedVisibilityScope,
                sharedElementKey = "favorite-$bookIdName",
                unselectedTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                onClick = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
            )
        }
    }
}
