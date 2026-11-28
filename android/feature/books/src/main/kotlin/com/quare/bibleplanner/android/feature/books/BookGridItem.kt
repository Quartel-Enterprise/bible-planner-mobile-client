package com.quare.bibleplanner.android.feature.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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

@Composable
internal fun BookGridItem(
    book: BookPresentationModel,
    searchQuery: String,
    transitionScopes: BooksTransitionScopes?,
    onEvent: (BooksUiEvent) -> Unit,
) {
    BookCard(
        book = book,
        transitionScopes = transitionScopes,
        onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                BookTypeIcon(
                    book = book,
                    transitionScopes = transitionScopes,
                    iconSize = 48.dp,
                    shape = RoundedCornerShape(0.dp),
                    iconAlpha = 0.4f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                )
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    BookFavoriteButton(
                        book = book,
                        transitionScopes = transitionScopes,
                        unselectedTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        onToggle = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(
                                top = 8.dp,
                                end = 8.dp,
                            ),
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
                    transitionScopes = transitionScopes,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BookProgressText(
                        book = book,
                        transitionScopes = transitionScopes,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    BookPercentageText(
                        book = book,
                        transitionScopes = transitionScopes,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                BookProgressIndicator(
                    book = book,
                    transitionScopes = transitionScopes,
                    height = 8.dp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
