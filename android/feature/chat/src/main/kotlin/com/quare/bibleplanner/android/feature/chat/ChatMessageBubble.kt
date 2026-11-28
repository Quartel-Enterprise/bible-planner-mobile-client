package com.quare.bibleplanner.android.feature.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.quare.bibleplanner.feature.chat.presentation.model.ChatMessageUiModel

private const val BUBBLE_MAX_WIDTH_FRACTION = 0.85f
private const val CARET = "▌"
private const val CARET_BLINK_MILLIS = 500
private const val CARET_ID = "caret"
private const val CARET_WIDTH_EM = 0.6f
private val userBubbleShape = RoundedCornerShape(
    topStart = 18.dp,
    topEnd = 18.dp,
    bottomEnd = 4.dp,
    bottomStart = 18.dp,
)
private val assistantBubbleShape = RoundedCornerShape(
    topStart = 4.dp,
    topEnd = 18.dp,
    bottomEnd = 18.dp,
    bottomStart = 18.dp,
)

@Composable
internal fun ChatMessageBubble(
    message: ChatMessageUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(BUBBLE_MAX_WIDTH_FRACTION),
            horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (message.isFromUser) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        shape = if (message.isFromUser) userBubbleShape else assistantBubbleShape,
                    ).padding(
                        horizontal = 15.dp,
                        vertical = 12.dp,
                    ),
            ) {
                Text(
                    text = message.annotatedText(),
                    inlineContent = if (message.isStreaming) caretInlineContent() else emptyMap(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isFromUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}

@Composable
private fun ChatMessageUiModel.annotatedText(): AnnotatedString = remember(text, isStreaming) {
    if (isStreaming) {
        buildAnnotatedString {
            append(text)
            appendInlineContent(
                id = CARET_ID,
                alternateText = CARET,
            )
        }
    } else {
        AnnotatedString(text)
    }
}

/*
 * Why: the caret's alpha is read only in graphicsLayer, so its blinking redraws the caret alone instead of
 * recomposing and laying out the whole streamed answer every frame.
 */
@Composable
private fun caretInlineContent(): Map<String, InlineTextContent> {
    val transition = rememberInfiniteTransition()
    val alpha = transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = CARET_BLINK_MILLIS),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val caretColor = MaterialTheme.colorScheme.primary
    val caretStyle = MaterialTheme.typography.bodyMedium
    return remember(alpha, caretColor, caretStyle) {
        mapOf(
            CARET_ID to InlineTextContent(
                placeholder = Placeholder(
                    width = CARET_WIDTH_EM.em,
                    height = 1.em,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                ),
            ) {
                Text(
                    text = CARET,
                    modifier = Modifier.graphicsLayer { this.alpha = alpha.value },
                    color = caretColor,
                    style = caretStyle,
                )
            },
        )
    }
}
