package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BookGridItemComponent(
    modifier: Modifier = Modifier,
    book: BookPresentationModel,
    searchQuery: String,
    screensSharedTransitionScope: SharedTransitionScope,
    screensAnimatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BooksUiEvent) -> Unit,
) {
    BookCard(
        modifier = modifier,
        book = book,
        onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
        sharedTransitionScope = screensSharedTransitionScope,
        animatedVisibilityScope = screensAnimatedVisibilityScope,
    ) {
        val bookIdName = book.id.name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
        ) {
            // Preview area / Icon area
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                BookTypeIcon(
                    book = book,
                    sharedTransitionScope = screensSharedTransitionScope,
                    animatedVisibilityScope = screensAnimatedVisibilityScope,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    iconSize = 48.dp,
                    shape = RoundedCornerShape(0.dp), // Full width at top
                    iconAlpha = 0.4f,
                )

                // Favorite button positioned in the corner
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    FavoriteIconButton(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 8.dp, end = 8.dp),
                        isFavorite = book.isFavorite,
                        sharedTransitionScope = screensSharedTransitionScope,
                        animatedVisibilityScope = screensAnimatedVisibilityScope,
                        sharedElementKey = "favorite-$bookIdName",
                        unselectedTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        onClick = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
            ) {
                BookTitleText(
                    book = book,
                    searchQuery = searchQuery,
                    sharedTransitionScope = screensSharedTransitionScope,
                    animatedVisibilityScope = screensAnimatedVisibilityScope,
                    style = MaterialTheme.typography.bodyMedium,
                )

                VerticalSpacer(4.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BookProgressNumbers(
                        book = book,
                        sharedTransitionScope = screensSharedTransitionScope,
                        animatedVisibilityScope = screensAnimatedVisibilityScope,
                        style = MaterialTheme.typography.labelSmall,
                    )

                    BookPercentageText(
                        progress = book.progress,
                        bookIdName = bookIdName,
                        sharedTransitionScope = screensSharedTransitionScope,
                        animatedVisibilityScope = screensAnimatedVisibilityScope,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                VerticalSpacer(8.dp)
                BookProgressBar(
                    progress = if (book.isCompleted) 1f else book.progress,
                    bookIdName = bookIdName,
                    sharedTransitionScope = screensSharedTransitionScope,
                    animatedVisibilityScope = screensAnimatedVisibilityScope,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    height = 8.dp,
                )
            }
        }
    }
}
