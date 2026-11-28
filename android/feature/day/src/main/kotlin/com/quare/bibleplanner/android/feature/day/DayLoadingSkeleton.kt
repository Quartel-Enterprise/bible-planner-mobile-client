package com.quare.bibleplanner.android.feature.day

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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private const val PASSAGE_ROW_COUNT = 3
private const val PANE_LINE_COUNT = 4
private const val PANE_LAST_LINE_FRACTION = 0.5f
private const val READING_WEIGHT = 2f
private const val STUDY_WEIGHT = 3f
private const val PULSE_MILLIS = 900
private const val PULSE_MIN_ALPHA = 0.4f
private val portraitContentMaxWidth = 600.dp

@Composable
internal fun DayLoadingSkeleton(
    isWide: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isWide) {
        WideSkeleton(
            onBackClick = onBackClick,
            modifier = modifier,
        )
    } else {
        NarrowSkeleton(modifier = modifier)
    }
}

@Composable
private fun NarrowSkeleton(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = portraitContentMaxWidth)
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DaySkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                shape = RoundedCornerShape(18.dp),
            )
            ButtonSkeleton()
            PassagesCardSkeleton()
            NotesSkeleton()
        }
    }
}

// Why: the day study panel opens beside the day only once it loads, so its place is held meanwhile.
@Composable
private fun WideSkeleton(
    onBackClick: () -> Unit,
    modifier: Modifier,
) {
    Row(modifier = modifier) {
        SkeletonPane(weight = READING_WEIGHT) {
            Column(
                modifier = Modifier
                    .widthIn(max = wideReadingMaxWidth)
                    .fillMaxWidth()
                    .fillMaxHeight(),
            ) {
                DayWideHeader(onBackClick = onBackClick) {
                    DayHeaderTitleSkeleton(horizontalAlignment = Alignment.Start)
                }
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PassagesCardSkeleton()
                    ButtonSkeleton()
                    NotesSkeleton()
                }
            }
        }
        VerticalDivider()
        SkeletonPane(weight = STUDY_WEIGHT) {
            Column(
                modifier = Modifier
                    .widthIn(max = wideReadingMaxWidth)
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StudyPaneSkeleton()
            }
        }
    }
}

@Composable
private fun RowScope.SkeletonPane(
    weight: Float,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight(),
        contentAlignment = Alignment.TopCenter,
    ) {
        content()
    }
}

@Composable
private fun ButtonSkeleton() {
    DaySkeletonBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(percent = 50),
    )
}

@Composable
private fun PassagesCardSkeleton() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        repeat(PASSAGE_ROW_COUNT) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DaySkeletonBox(
                    modifier = Modifier
                        .width(120.dp)
                        .height(18.dp),
                )
                DaySkeletonBox(
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(6.dp),
                )
            }
            if (index < PASSAGE_ROW_COUNT - 1) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun NotesSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DaySkeletonBox(
            modifier = Modifier
                .width(80.dp)
                .height(20.dp),
        )
        DaySkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
private fun StudyPaneSkeleton() {
    DaySkeletonBox(
        modifier = Modifier
            .width(160.dp)
            .height(24.dp),
    )
    DaySkeletonBox(
        modifier = Modifier
            .width(100.dp)
            .height(16.dp),
    )
    DaySkeletonBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        shape = RoundedCornerShape(8.dp),
    )
    repeat(PANE_LINE_COUNT) {
        DaySkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
        )
    }
    DaySkeletonBox(
        modifier = Modifier
            .fillMaxWidth(PANE_LAST_LINE_FRACTION)
            .height(14.dp),
    )
}

@Composable
internal fun DaySkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    val pulse by rememberInfiniteTransition(label = "day_skeleton").animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MIN_ALPHA,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_MILLIS),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "day_skeleton_alpha",
    )
    Box(
        modifier = modifier
            .graphicsLayer { alpha = pulse }
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = shape,
            ),
    )
}
