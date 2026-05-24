package com.quare.bibleplanner.feature.readingplan.presentation.component.week

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.week_progress_summary
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel
import com.quare.bibleplanner.ui.component.icon.ArrowRotationIcon
import com.quare.bibleplanner.ui.component.progress.AppLinearProgressBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.WeekRow(
    animatedContentScope: AnimatedContentScope,
    weekPresentation: WeekPlanPresentationModel,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val weekNumber = weekPresentation.weekPlan.number
    val totalDays = weekPresentation.totalDays
    val targetProgress = if (totalDays > 0) {
        weekPresentation.readDaysCount.toFloat() / totalDays.toFloat()
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "weekProgressAnimation",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    onEvent(ReadingPlanUiEvent.OnWeekExpandClick(weekNumber))
                },
            ).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            WeekNumberComponent(
                weekNumber = weekNumber,
                animatedContentScope = animatedContentScope,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(
                        Res.string.week_progress_summary,
                        weekPresentation.readDaysCount,
                        totalDays,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppLinearProgressBar(
                    modifier = Modifier.weight(1f),
                    progress = animatedProgress,
                )
            }
        }
        ArrowRotationIcon(
            modifier = Modifier.size(24.dp),
            isUp = weekPresentation.isExpanded,
        )
    }
}
