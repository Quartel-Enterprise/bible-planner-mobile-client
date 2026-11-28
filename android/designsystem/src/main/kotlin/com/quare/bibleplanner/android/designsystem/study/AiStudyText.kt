package com.quare.bibleplanner.android.designsystem.study

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.R

private const val DEFAULT_COLLAPSED_MAX_LINES = 3
private const val ELLIPSIS = "… "
private const val TOGGLE_SEPARATOR = "  "
private const val TOGGLE_TAG = "expandable_text_toggle"

@Composable
fun AiDisclaimerText(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = stringResource(R.string.study_ai_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Why: long texts start collapsed (three lines by default), with an inline "Show more" link at the end of the last one.
@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    collapsedMaxLines: Int = DEFAULT_COLLAPSED_MAX_LINES,
) {
    var isExpanded by rememberSaveable(text) { mutableStateOf(false) }
    val showMore = stringResource(R.string.study_show_more)
    val showLess = stringResource(R.string.study_show_less)
    var lastVisibleCharIndex by remember(text) { mutableIntStateOf(0) }
    var hasOverflow by remember(text) { mutableStateOf(false) }
    var truncatedAtWidth by remember(text) { mutableIntStateOf(0) }
    val toggleLink = LinkAnnotation.Clickable(
        tag = TOGGLE_TAG,
        styles = TextLinkStyles(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            ),
        ),
    ) { isExpanded = !isExpanded }
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

            else -> append(text)
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
                val layoutWidth = result.layoutInput.constraints.maxWidth
                when {
                    isExpanded -> Unit

                    result.hasVisualOverflow -> {
                        hasOverflow = true
                        truncatedAtWidth = layoutWidth
                        lastVisibleCharIndex = result.getLineEnd(
                            lineIndex = collapsedMaxLines - 1,
                            visibleEnd = true,
                        )
                    }

                    // Why: the study panel resizes, and a cut made at a narrower width hides text that now fits.
                    hasOverflow && layoutWidth != truncatedAtWidth -> {
                        hasOverflow = false
                        lastVisibleCharIndex = 0
                    }
                }
            },
            modifier = Modifier.animateContentSize(),
        )
    }
}
