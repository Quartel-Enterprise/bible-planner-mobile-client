package com.quare.bibleplanner.android.feature.read

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

private const val BAR_COUNT = 3
private const val MIN_FRACTION = 0.3f
private const val PAUSED_FRACTION = 0.45f
private val indicatorWidth = 14.dp
private val indicatorHeight = 14.dp
private val barWidth = 3.dp
private val barDurationsMillis = listOf(900, 800, 1_000)

/*
 * Why: the bars only move while playing, and their heights are read while drawing, so a paused
 * player runs no animation and a playing one redraws without recomposing.
 */
@Composable
internal fun ListeningEqualizerIndicator(
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
) {
    val fractions = if (isAnimating) rememberAnimatedFractions() else rememberPausedFractions()
    val color = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier.size(
            width = indicatorWidth,
            height = indicatorHeight,
        ),
    ) {
        val barWidthPx = barWidth.toPx()
        val gap = (size.width - barWidthPx * BAR_COUNT) / (BAR_COUNT - 1)
        fractions.forEachIndexed { index, fraction ->
            val barHeight = size.height * fraction.value
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = index * (barWidthPx + gap),
                    y = size.height - barHeight,
                ),
                size = Size(
                    width = barWidthPx,
                    height = barHeight,
                ),
                cornerRadius = CornerRadius(barWidthPx / 2),
            )
        }
    }
}

@Composable
private fun rememberAnimatedFractions(): List<State<Float>> {
    val transition = rememberInfiniteTransition()
    return barDurationsMillis.mapIndexed { index, durationMillis ->
        transition.animateFloat(
            initialValue = if (index % 2 == 0) MIN_FRACTION else 1f,
            targetValue = if (index % 2 == 0) 1f else MIN_FRACTION,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis),
                repeatMode = RepeatMode.Reverse,
            ),
        )
    }
}

@Composable
private fun rememberPausedFractions(): List<State<Float>> = remember {
    List(BAR_COUNT) { mutableFloatStateOf(PAUSED_FRACTION) }
}
