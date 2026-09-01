package com.quare.bibleplanner.feature.chat.presentation.component

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.chat.presentation.model.ChatMessageUiModel

private const val BUBBLE_MAX_WIDTH_FRACTION = 0.85f
private const val CARET = "▌"
private const val CARET_BLINK_MILLIS = 500
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
private fun ChatMessageUiModel.annotatedText() = buildAnnotatedString {
    append(text)
    if (isStreaming) {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary.copy(alpha = caretAlpha()))) {
            append(CARET)
        }
    }
}

@Composable
private fun caretAlpha(): Float {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = CARET_BLINK_MILLIS),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    return alpha
}
