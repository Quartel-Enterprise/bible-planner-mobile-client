package com.quare.bibleplanner.feature.readingplan.presentation.component.week

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.week_progress_complete
import org.jetbrains.compose.resources.stringResource

private const val DAY_PROGRESS_ANIMATION_DURATION_MS = 300

@Composable
internal fun WeekProgressLabel(
    readDaysCount: Int,
    totalDays: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        AnimatedContent(
            targetState = readDaysCount,
            transitionSpec = {
                getDayProgressAnimation()
            },
            label = "readDaysCountAnimation",
        ) { count ->
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = "/$totalDays",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = " ${stringResource(Res.string.week_progress_complete)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun getDayProgressAnimation(): ContentTransform = (
    fadeIn(animationSpec = tween(DAY_PROGRESS_ANIMATION_DURATION_MS)) + slideInVertically(
        animationSpec = tween(DAY_PROGRESS_ANIMATION_DURATION_MS),
        initialOffsetY = { it },
    )
) togetherWith (
    fadeOut(animationSpec = tween(DAY_PROGRESS_ANIMATION_DURATION_MS)) + slideOutVertically(
        animationSpec = tween(DAY_PROGRESS_ANIMATION_DURATION_MS),
        targetOffsetY = { -it },
    )
)
