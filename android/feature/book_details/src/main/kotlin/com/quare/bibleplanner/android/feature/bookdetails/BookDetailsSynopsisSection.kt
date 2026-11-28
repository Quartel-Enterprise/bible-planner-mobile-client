package com.quare.bibleplanner.android.feature.bookdetails

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val COLLAPSED_MAX_LINES = 3
private const val ELLIPSIS = "… "
private const val TOGGLE_SEPARATOR = "  "
private const val TOGGLE_TAG = "book_details_synopsis_toggle"

@Composable
internal fun BookDetailsSynopsisSection(
    synopsis: String,
    bookGroupName: String,
    bookGroupSharedKey: String,
    isExpanded: Boolean,
    transitionScopes: BookDetailsTransitionScopes?,
    onToggleExpanded: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.book_details_synopsis),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = bookGroupName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .sharedElementOf(
                            transitionScopes = transitionScopes,
                            key = bookGroupSharedKey,
                        ).padding(
                            horizontal = 12.dp,
                            vertical = 4.dp,
                        ),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SynopsisText(
            text = synopsis,
            isExpanded = isExpanded,
            onToggleExpanded = onToggleExpanded,
        )
    }
}

@Composable
private fun SynopsisText(
    text: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val showMore = stringResource(R.string.book_details_show_more)
    val showLess = stringResource(R.string.book_details_show_less)
    BoxWithConstraints {
        // Why: the cut point depends on the width, so a resize (foldable, multi window) measures it again.
        CollapsibleSynopsisText(
            text = text,
            isExpanded = isExpanded,
            onToggleExpanded = onToggleExpanded,
            showMore = showMore,
            showLess = showLess,
            maxWidth = maxWidth,
        )
    }
}

@Composable
private fun CollapsibleSynopsisText(
    text: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    showMore: String,
    showLess: String,
    maxWidth: Dp,
) {
    var lastVisibleCharIndex by remember(text, maxWidth) { mutableIntStateOf(0) }
    var hasOverflow by remember(text, maxWidth) { mutableStateOf(false) }
    val toggleLink = LinkAnnotation.Clickable(
        tag = TOGGLE_TAG,
        styles = TextLinkStyles(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            ),
        ),
    ) { onToggleExpanded() }
    val annotatedText = buildAnnotatedString {
        when {
            isExpanded -> {
                append(text)
                append(TOGGLE_SEPARATOR)
                withLink(toggleLink) { append(showLess) }
            }

            hasOverflow && lastVisibleCharIndex > 0 -> {
                // Why: the collapsed text ends early enough for the ellipsis and "Show more" to fit in its last line.
                val truncated = text
                    .take(lastVisibleCharIndex)
                    .dropLast(showMore.length + ELLIPSIS.length)
                    .trimEnd()
                append(truncated)
                append(ELLIPSIS)
                withLink(toggleLink) { append(showMore) }
            }

            else -> append(text)
        }
    }
    SelectionContainer {
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (isExpanded) Int.MAX_VALUE else COLLAPSED_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!isExpanded && result.hasVisualOverflow) {
                    hasOverflow = true
                    lastVisibleCharIndex = result.getLineEnd(
                        lineIndex = COLLAPSED_MAX_LINES - 1,
                        visibleEnd = true,
                    )
                }
            },
            modifier = Modifier.animateContentSize(),
        )
    }
}
