package com.quare.bibleplanner.feature.bookdetails.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import bibleplanner.feature.book_details.generated.resources.Res
import bibleplanner.feature.book_details.generated.resources.show_less
import bibleplanner.feature.book_details.generated.resources.show_more
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SynopsisText(
    synopsis: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showMore = stringResource(Res.string.show_more)
    val showLess = stringResource(Res.string.show_less)
    var lastVisibleCharIndex by remember(synopsis) { mutableStateOf(0) }
    var hasOverflow by remember(synopsis) { mutableStateOf(false) }
    val toggleLink = LinkAnnotation.Clickable(
        tag = TOGGLE_TAG,
        styles = TextLinkStyles(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            ),
        ),
    ) { onToggleExpanded() }
    val text = buildAnnotatedString {
        when {
            isExpanded -> {
                append(synopsis)
                append(TOGGLE_SEPARATOR)
                withLink(toggleLink) { append(showLess) }
            }

            hasOverflow && lastVisibleCharIndex > 0 -> {
                val truncated = synopsis
                    .take(lastVisibleCharIndex)
                    .dropLast(showMore.length + ELLIPSIS.length)
                    .trimEnd()
                append(truncated)
                append(ELLIPSIS)
                withLink(toggleLink) { append(showMore) }
            }

            else -> {
                append(synopsis)
            }
        }
    }
    SelectionContainer(modifier = modifier) {
        Text(
            text = text,
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

private const val COLLAPSED_MAX_LINES = 3
private const val ELLIPSIS = "… "
private const val TOGGLE_SEPARATOR = "  "
private const val TOGGLE_TAG = "synopsis_toggle"
