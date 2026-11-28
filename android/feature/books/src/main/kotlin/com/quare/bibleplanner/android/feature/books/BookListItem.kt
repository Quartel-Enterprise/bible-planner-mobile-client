package com.quare.bibleplanner.android.feature.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

@Composable
internal fun BookListItem(
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
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookTypeIcon(
                book = book,
                transitionScopes = transitionScopes,
                iconSize = 20.dp,
                shape = RoundedCornerShape(8.dp),
                iconAlpha = 0.5f,
                modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                BookTitleText(
                    book = book,
                    searchQuery = searchQuery,
                    transitionScopes = transitionScopes,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BookProgressText(
                        book = book,
                        transitionScopes = transitionScopes,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
                    )
                    BookPercentageText(
                        book = book,
                        transitionScopes = transitionScopes,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                BookProgressIndicator(
                    book = book,
                    transitionScopes = transitionScopes,
                    height = 4.dp,
                )
            }
            BookFavoriteButton(
                book = book,
                transitionScopes = transitionScopes,
                unselectedTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                onToggle = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                modifier = Modifier.size(40.dp),
            )
        }
    }
}
