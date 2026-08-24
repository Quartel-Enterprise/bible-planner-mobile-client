package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent

/**
 * Repeats the bottom bar's controls at the end of the text, naming the chapters this time: at the
 * end of a chapter the next step is the decision, not a bar the reader has scrolled past. A null
 * [suggestions] drops the arrows altogether, leaving the read pill alone across the row.
 */
@Composable
internal fun ChapterEndNavigation(
    suggestions: ReadNavigationSuggestionsModel?,
    isRead: Boolean,
    onReadClick: () -> Unit,
    onEvent: (ReadUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        suggestions?.let {
            ChapterEndButton(
                suggestion = it.previous,
                isNext = false,
                onEvent = onEvent,
            )
        }
        ReadStatusPill(
            modifier = Modifier.weight(1f),
            isRead = isRead,
            onClick = onReadClick,
        )
        suggestions?.let {
            ChapterEndButton(
                suggestion = it.next,
                isNext = true,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun ChapterEndButton(
    suggestion: ReadNavigationSuggestionModel?,
    isNext: Boolean,
    onEvent: (ReadUiEvent) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ChapterNavigationButton(
            suggestion = suggestion,
            isNext = isNext,
            onClick = onEvent,
        )
        suggestion?.let {
            Text(
                text = "${it.bookId.getBookName()} ${it.chapterNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
