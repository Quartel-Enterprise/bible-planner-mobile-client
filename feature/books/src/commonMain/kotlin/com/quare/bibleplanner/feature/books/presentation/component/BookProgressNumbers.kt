package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.quare.bibleplanner.core.books.presentation.model.BookPresentationModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BookProgressNumbers(
    book: BookPresentationModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall,
) {
    val progressText = book.chapterProgressText // "12 / 50"
    val parts = progressText.split(" ")
    val numerator = parts.getOrNull(0) ?: ""
    val slash = parts.getOrNull(1) ?: " / "
    val denominator = parts.getOrNull(2) ?: ""
    val textColor = if (book.isCompleted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    with(sharedTransitionScope) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = numerator,
                style = style,
                color = textColor,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "numerator-${book.id.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            )
            Text(
                text = " $slash ",
                style = style,
                color = textColor,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "slash-${book.id.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            )
            Text(
                text = denominator,
                style = style,
                color = textColor,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "denominator-${book.id.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            )
        }
    }
}
