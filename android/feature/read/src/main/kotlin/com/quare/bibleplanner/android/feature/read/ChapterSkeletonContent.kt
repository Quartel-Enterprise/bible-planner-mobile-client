package com.quare.bibleplanner.android.feature.read

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private const val VERSE_SKELETON_COUNT = 8
private const val PULSE_MILLIS = 900
private const val PULSE_MIN_ALPHA = 0.4f
private val skeletonShape = RoundedCornerShape(8.dp)
private const val CHAPTER_SKELETON_KEY = "chapter-skeleton"

internal const val CHAPTER_SKELETON_ITEM_COUNT = VERSE_SKELETON_COUNT + 1
private val verseLineWidthFractions = listOf(
    listOf(1f, 0.96f, 0.58f),
    listOf(1f, 0.71f),
    listOf(1f, 0.93f, 0.82f),
    listOf(1f, 0.64f),
)
private val headerVerticalPadding = 24.dp
private val bookNameWidth = 120.dp
private val bookNameHeight = 15.dp
private val chapterNumberWidth = 72.dp
private val chapterNumberHeight = 56.dp
private val verseNumberWidth = 18.dp
private val verseLineHeight = 14.dp
private val verseLineSpacing = 14.dp
private val verseVerticalPadding = 6.dp

// Why: one pulse drives every row, so lazily composed rows stay in step and only one animation runs.
internal fun LazyListScope.chapterSkeletonContent(
    position: ChapterSkeletonPosition,
    pulse: () -> Float,
) {
    item(key = "$CHAPTER_SKELETON_KEY-${position.name}-header") {
        ChapterHeaderSkeleton(pulse = pulse)
    }
    items(
        count = VERSE_SKELETON_COUNT,
        key = { index -> "$CHAPTER_SKELETON_KEY-${position.name}-verse-$index" },
    ) { index ->
        VerseSkeletonRow(
            lineWidthFractions = verseLineWidthFractions[index % verseLineWidthFractions.size],
            pulse = pulse,
        )
    }
}

@Composable
private fun ChapterHeaderSkeleton(pulse: () -> Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = pulse() }
            .padding(vertical = headerVerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ReadSkeletonBox(
            modifier = Modifier
                .width(bookNameWidth)
                .height(bookNameHeight),
        )
        ReadSkeletonBox(
            modifier = Modifier
                .width(chapterNumberWidth)
                .height(chapterNumberHeight),
        )
    }
}

@Composable
private fun VerseSkeletonRow(
    lineWidthFractions: List<Float>,
    pulse: () -> Float,
) {
    Row(
        modifier = Modifier
            .graphicsLayer { alpha = pulse() }
            .padding(vertical = verseVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        ReadSkeletonBox(
            modifier = Modifier
                .width(verseNumberWidth)
                .height(verseLineHeight),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(verseLineSpacing),
        ) {
            lineWidthFractions.forEach { fraction ->
                ReadSkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(verseLineHeight),
                )
            }
        }
    }
}

@Composable
internal fun rememberSkeletonPulse(): State<Float> = rememberInfiniteTransition(label = "read_skeleton").animateFloat(
    initialValue = 1f,
    targetValue = PULSE_MIN_ALPHA,
    animationSpec = infiniteRepeatable(
        animation = tween(PULSE_MILLIS),
        repeatMode = RepeatMode.Reverse,
    ),
    label = "read_skeleton_alpha",
)

@Composable
internal fun ReadSkeletonBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = skeletonShape,
            ),
    )
}
