package com.quare.bibleplanner.android.feature.accountdetails

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private const val PULSE_MIN_ALPHA = 0.4f
private const val PULSE_MAX_ALPHA = 1f
private const val PULSE_DURATION_MILLIS = 800

@Composable
internal fun PlaceholderBox(modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "placeholderPulse").animateFloat(
        initialValue = PULSE_MAX_ALPHA,
        targetValue = PULSE_MIN_ALPHA,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_DURATION_MILLIS),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "placeholderAlpha",
    )
    Box(
        modifier = modifier
            .graphicsLayer { alpha = pulse }
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.small,
            ),
    )
}
