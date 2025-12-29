package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.DayModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.AnimatedPlannedReadDateComponent(
    modifier: Modifier = Modifier,
    day: DayModel,
    animatedContentScope: AnimatedContentScope,
    weekNumber: Int,
    dayNumber: Int,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = day.plannedReadDate,
    ) { plannedReadDate ->
        if (plannedReadDate != null) {
            PlannedReadDateComponent(
                modifier = Modifier.padding(start = 16.dp),
                plannedReadDate = plannedReadDate,
                isRead = day.isRead,
                animatedContentScope = animatedContentScope,
                weekNumber = weekNumber,
                dayNumber = dayNumber,
            )
        }
    }
}
