package com.quare.bibleplanner.feature.chat.presentation.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_thinking
import com.quare.bibleplanner.ui.component.shimmer.shimmer
import org.jetbrains.compose.resources.stringResource

private const val SPARKLE_CYCLE_MILLIS = 1900
private const val SPARKLE_MIN_SCALE = 0.82f
private const val SPARKLE_MAX_SCALE = 1.12f
private const val FULL_TURN_DEGREES = 360f
private val sparkleSize = 18.dp

@Composable
internal fun ChatThinkingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SPARKLE_CYCLE_MILLIS),
            repeatMode = RepeatMode.Restart,
        ),
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            modifier = Modifier
                .size(sparkleSize)
                .rotate(progress * FULL_TURN_DEGREES)
                .scale(SPARKLE_MIN_SCALE + (SPARKLE_MAX_SCALE - SPARKLE_MIN_SCALE) * progress),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(Res.string.chat_thinking),
            modifier = Modifier.shimmer(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
