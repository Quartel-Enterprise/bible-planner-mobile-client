package com.quare.bibleplanner.ui.component.progress

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookProgressBar(
    progress: Float,
    bookIdName: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    with(sharedTransitionScope) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier
                .height(height)
                .clip(CircleShape)
                .sharedBounds(
                    rememberSharedContentState(key = "progress-bar-$bookIdName"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
            gapSize = (-15).dp,
            drawStopIndicator = {},
        )
    }
}
