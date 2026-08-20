package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.dismiss_ruler
import org.jetbrains.compose.resources.stringResource

private const val SCRIM_ALPHA = 0.45f
private val bandExtraHeight = 14.dp
private val initialBandOffset = 24.dp
private val handleWidth = 22.dp
private val handleHeight = 26.dp
private val handleGripWidth = 10.dp
private val handleGripHeight = 1.5.dp
private val handleInset = 6.dp
private val dismissButtonSize = 20.dp
private val dismissIconSize = 12.dp
private val dismissInset = 8.dp

/**
 * Dims everything but a band of the reader's chosen height, so the eye has somewhere to sit while
 * scrolling. The band starts near the top and is dragged by the grip on its left, because where the
 * eye wants the line is not something the app can guess.
 *
 * Only the grip and the close button take taps: the verses under the dimmed area stay reachable.
 */
@Composable
internal fun ReadingRulerOverlay(
    lineHeight: Dp,
    lines: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA)
    val bandHeight = lineHeight * lines + bandExtraHeight
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxOffsetPx = with(density) { (maxHeight - bandHeight).coerceAtLeast(0.dp).toPx() }
        var bandOffsetPx by remember { mutableFloatStateOf(with(density) { initialBandOffset.toPx() }) }
        val bandOffset = with(density) { bandOffsetPx.coerceIn(0f, maxOffsetPx).toDp() }
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bandOffset)
                    .background(scrimColor),
            )
            RulerBand(
                height = bandHeight,
                onDrag = { delta -> bandOffsetPx = (bandOffsetPx + delta).coerceIn(0f, maxOffsetPx) },
                onDismiss = onDismiss,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(scrimColor),
            )
        }
    }
}

@Composable
private fun RulerBand(
    height: Dp,
    onDrag: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.CenterStart,
    ) {
        DragHandle(
            modifier = Modifier.padding(start = handleInset),
            onDrag = onDrag,
        )
        DismissButton(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = dismissInset),
            onDismiss = onDismiss,
        )
    }
}

/** Filled rather than bare, so it never reads as part of the verse it sits over. */
@Composable
private fun DismissButton(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(dismissButtonSize),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp,
        onClick = onDismiss,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(dismissIconSize),
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.dismiss_ruler),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun DragHandle(
    onDrag: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(handleWidth)
            .height(handleHeight)
            .draggable(
                state = rememberDraggableState(onDelta = onDrag),
                orientation = Orientation.Vertical,
            ),
        shape = RoundedCornerShape(7.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .width(handleGripWidth)
                        .height(handleGripHeight)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(MaterialTheme.colorScheme.onPrimary),
                )
            }
        }
    }
}
