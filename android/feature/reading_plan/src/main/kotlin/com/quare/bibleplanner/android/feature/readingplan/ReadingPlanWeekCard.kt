package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekGroup
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel
import com.quare.bibleplanner.ui.utils.transition.TransitionScopes
import com.quare.bibleplanner.ui.utils.transition.sharedElementOf

private const val PROGRESS_ANIMATION_MILLIS = 400
private const val EXPANDED_ARROW_ROTATION = 180f
private const val COLLAPSED_ARROW_ROTATION = 0f

@Composable
internal fun ReadingPlanWeekCard(
    week: WeekPlanPresentationModel,
    flashTargetGlobalIndex: Int,
    transitionScopes: TransitionScopes?,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = if (week.group == WeekGroup.Current) {
            BorderStroke(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            null
        },
    ) {
        WeekHeaderRow(
            week = week,
            transitionScopes = transitionScopes,
            onEvent = onEvent,
        )
        AnimatedVisibility(
            visible = week.isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                week.dayPlans.forEachIndexed { index, dayPlan ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                    ReadingPlanDayItem(
                        weekNumber = week.weekPlan.number,
                        dayPlan = dayPlan,
                        isFlashing = flashTargetGlobalIndex != 0 && dayPlan.globalDayIndex == flashTargetGlobalIndex,
                        transitionScopes = transitionScopes,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekHeaderRow(
    week: WeekPlanPresentationModel,
    transitionScopes: TransitionScopes?,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val weekNumber = week.weekPlan.number
    val totalDays = week.totalDays
    val isCurrent = week.group == WeekGroup.Current
    val animatedProgress by animateFloatAsState(
        targetValue = if (totalDays > 0) week.readDaysCount.toFloat() / totalDays.toFloat() else 0f,
        animationSpec = tween(
            durationMillis = PROGRESS_ANIMATION_MILLIS,
            easing = LinearEasing,
        ),
        label = "weekProgressAnimation",
    )
    val animatedReadDays by animateIntAsState(
        targetValue = week.readDaysCount,
        label = "weekReadDaysAnimation",
    )
    val summary = stringResource(R.string.reading_plan_week_progress_summary, animatedReadDays, totalDays)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEvent(ReadingPlanUiEvent.OnWeekExpandClick(weekNumber)) }
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (week.group == WeekGroup.Completed) {
            CompletedWeekBadge()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.sharedElementOf(
                    transitionScopes = transitionScopes,
                    key = SharedTransitionAnimationUtils.buildWeekNumberId(weekNumber),
                ),
                text = stringResource(R.string.reading_plan_week_number, weekNumber),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isCurrent) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isCurrent) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                        gapSize = (-15).dp,
                        drawStopIndicator = {},
                    )
                }
            }
        }
        ExpandArrowIcon(isExpanded = week.isExpanded)
    }
}

@Composable
private fun ExpandArrowIcon(isExpanded: Boolean) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) EXPANDED_ARROW_ROTATION else COLLAPSED_ARROW_ROTATION,
        label = "weekArrowRotation",
    )
    Icon(
        modifier = Modifier
            .size(24.dp)
            .rotate(rotation),
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = stringResource(
            if (isExpanded) R.string.reading_plan_collapse else R.string.reading_plan_expand,
        ),
    )
}

@Composable
private fun CompletedWeekBadge() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondary,
        )
    }
}
