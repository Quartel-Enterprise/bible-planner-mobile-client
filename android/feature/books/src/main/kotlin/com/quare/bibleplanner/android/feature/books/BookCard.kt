package com.quare.bibleplanner.android.feature.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.utils.getShadowClip
import com.quare.bibleplanner.ui.utils.SharedTransitionModifierFactory
import com.quare.bibleplanner.ui.utils.highlightText

private val cardShape = RoundedCornerShape(16.dp)
private val cardShadowClip = getShadowClip(cardShape)

@Composable
internal fun BookCard(
    book: BookPresentationModel,
    transitionScopes: BooksTransitionScopes?,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .sharedBoundsOf(
                transitionScopes = transitionScopes,
                key = "book-${book.id.name}",
                clipInOverlayDuringTransition = cardShadowClip,
            ),
        shape = cardShape,
        content = content,
    )
}

@Composable
internal fun BookTypeIcon(
    book: BookPresentationModel,
    transitionScopes: BooksTransitionScopes?,
    iconSize: Dp,
    shape: Shape,
    iconAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (book.isCompleted) {
        colorScheme.primaryContainer.copy(alpha = if (iconAlpha > 0.4f) 0.5f else 1.0f)
    } else {
        colorScheme.surfaceVariant.copy(alpha = if (iconAlpha > 0.4f) 0.5f else 0.3f)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .sharedBoundsOf(
                transitionScopes = transitionScopes,
                key = "icon-box-${book.id.name}",
            ).background(backgroundColor),
    ) {
        Icon(
            imageVector = if (book.isCompleted) Icons.Default.Check else Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint = if (book.isCompleted || book.progress > 0f) {
                colorScheme.primary
            } else {
                colorScheme.onSurfaceVariant.copy(alpha = iconAlpha)
            },
            modifier = Modifier
                .size(iconSize)
                .sharedElementOf(
                    transitionScopes = transitionScopes,
                    key = "icon-${book.id.name}",
                ),
        )
    }
}

@Composable
internal fun BookTitleText(
    book: BookPresentationModel,
    searchQuery: String,
    transitionScopes: BooksTransitionScopes?,
    style: TextStyle,
) {
    val bookName = book.name
    Text(
        modifier = if (transitionScopes == null) {
            Modifier
        } else {
            SharedTransitionModifierFactory.getBookNameSharedTransitionModifier(
                sharedTransitionScope = transitionScopes.sharedTransitionScope,
                animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
                bookName = bookName,
            )
        },
        text = bookName.highlightText(
            query = searchQuery,
            highlightStyle = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                background = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            ),
        ),
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
internal fun BookProgressText(
    book: BookPresentationModel,
    transitionScopes: BooksTransitionScopes?,
    style: TextStyle,
) {
    val parts = book.chapterProgressText.split(" ")
    val bookIdName = book.id.name
    val textColor = if (book.isCompleted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = parts.getOrNull(0).orEmpty(),
            style = style,
            color = textColor,
            modifier = Modifier.sharedElementOf(
                transitionScopes = transitionScopes,
                key = "numerator-$bookIdName",
            ),
        )
        Text(
            text = " ${parts.getOrNull(1) ?: " / "} ",
            style = style,
            color = textColor,
            modifier = Modifier.sharedElementOf(
                transitionScopes = transitionScopes,
                key = "slash-$bookIdName",
            ),
        )
        Text(
            text = parts.getOrNull(2).orEmpty(),
            style = style,
            color = textColor,
            modifier = Modifier.sharedElementOf(
                transitionScopes = transitionScopes,
                key = "denominator-$bookIdName",
            ),
        )
    }
}

@Composable
internal fun BookPercentageText(
    book: BookPresentationModel,
    transitionScopes: BooksTransitionScopes?,
    style: TextStyle,
) {
    val bookIdName = book.id.name
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = (book.progress * 100).toInt().toString(),
            style = style,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.sharedBoundsOf(
                transitionScopes = transitionScopes,
                key = "percentage-$bookIdName",
            ),
        )
        Text(
            text = "%",
            style = style,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.sharedBoundsOf(
                transitionScopes = transitionScopes,
                key = "percentage-symbol-$bookIdName",
            ),
        )
    }
}

@Composable
internal fun BookProgressIndicator(
    book: BookPresentationModel,
    transitionScopes: BooksTransitionScopes?,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = { if (book.isCompleted) 1f else book.progress },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .sharedBoundsOf(
                transitionScopes = transitionScopes,
                key = "progress-bar-${book.id.name}",
            ),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round,
        gapSize = (-15).dp,
        drawStopIndicator = {},
    )
}

@Composable
internal fun BookFavoriteButton(
    book: BookPresentationModel,
    transitionScopes: BooksTransitionScopes?,
    unselectedTint: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconToggleButton(
        checked = book.isFavorite,
        onCheckedChange = { onToggle() },
        modifier = modifier,
    ) {
        Icon(
            imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = stringResource(R.string.books_favorite),
            tint = if (book.isFavorite) MaterialTheme.colorScheme.error else unselectedTint,
            modifier = Modifier.sharedElementOf(
                transitionScopes = transitionScopes,
                key = "favorite-${book.id.name}",
            ),
        )
    }
}
