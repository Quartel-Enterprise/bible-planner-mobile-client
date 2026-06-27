package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.AnimatedDaysList(
    animatedContentScope: AnimatedContentScope,
    weekPresentation: WeekPlanPresentationModel,
    flashTargetGlobalIndex: Int,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    AnimatedVisibility(
        visible = weekPresentation.isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column {
            weekPresentation.dayPlans.forEachIndexed { index, dayPlan ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
                DayItem(
                    weekNumber = weekPresentation.weekPlan.number,
                    dayPlan = dayPlan,
                    isFlashing = flashTargetGlobalIndex != 0 && dayPlan.globalDayIndex == flashTargetGlobalIndex,
                    onEvent = onEvent,
                    animatedContentScope = animatedContentScope,
                )
            }
        }
    }
}
