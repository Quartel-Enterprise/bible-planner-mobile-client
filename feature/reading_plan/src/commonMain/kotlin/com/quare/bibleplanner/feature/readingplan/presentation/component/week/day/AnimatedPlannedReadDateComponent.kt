package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.model.DayPlanPresentationModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.AnimatedPlannedReadDateComponent(
    modifier: Modifier = Modifier,
    dayPlan: DayPlanPresentationModel,
    animatedContentScope: AnimatedContentScope,
    weekNumber: Int,
    dayNumber: Int,
) {
    val day = dayPlan.day
    AnimatedContent(
        modifier = modifier,
        targetState = day.plannedReadDate,
    ) { plannedReadDate ->
        if (plannedReadDate != null) {
            PlannedReadDateComponent(
                modifier = Modifier.padding(start = 16.dp),
                plannedReadDate = plannedReadDate,
                isRead = day.isRead,
                shouldShowYear = dayPlan.shouldShowYear,
                animatedContentScope = animatedContentScope,
                weekNumber = weekNumber,
                dayNumber = dayNumber,
            )
        }
    }
}
