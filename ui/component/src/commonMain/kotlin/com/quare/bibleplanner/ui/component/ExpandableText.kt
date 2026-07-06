package com.quare.bibleplanner.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.show_less
import bibleplanner.ui.component.generated.resources.show_more
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    collapsedMaxLines: Int = DEFAULT_COLLAPSED_MAX_LINES,
) {
    var isExpanded by rememberSaveable(text) { mutableStateOf(false) }
    ExpandableText(
        text = text,
        isExpanded = isExpanded,
        onToggleExpanded = { isExpanded = !isExpanded },
        modifier = modifier,
        style = style,
        color = color,
        collapsedMaxLines = collapsedMaxLines,
    )
}

@Composable
fun ExpandableText(
    text: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    collapsedMaxLines: Int = DEFAULT_COLLAPSED_MAX_LINES,
) {
    val showMore = stringResource(Res.string.show_more)
    val showLess = stringResource(Res.string.show_less)
    var lastVisibleCharIndex by remember(text) { mutableStateOf(0) }
    var hasOverflow by remember(text) { mutableStateOf(false) }
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
                val truncated = text
                    .take(lastVisibleCharIndex)
                    .dropLast(showMore.length + ELLIPSIS.length)
                    .trimEnd()
                append(truncated)
                append(ELLIPSIS)
                withLink(toggleLink) { append(showMore) }
            }

            else -> {
                append(text)
            }
        }
    }
    SelectionContainer(modifier = modifier) {
        Text(
            text = annotatedText,
            style = style,
            color = color,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!isExpanded && result.hasVisualOverflow) {
                    hasOverflow = true
                    lastVisibleCharIndex = result.getLineEnd(
                        lineIndex = collapsedMaxLines - 1,
                        visibleEnd = true,
                    )
                }
            },
            modifier = Modifier.animateContentSize(),
        )
    }
}

private const val DEFAULT_COLLAPSED_MAX_LINES = 3
private const val ELLIPSIS = "… "
private const val TOGGLE_SEPARATOR = "  "
private const val TOGGLE_TAG = "expandable_text_toggle"
