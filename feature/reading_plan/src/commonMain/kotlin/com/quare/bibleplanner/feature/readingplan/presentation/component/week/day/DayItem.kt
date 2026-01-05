package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.readingplan.presentation.model.DayPlanPresentationModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.DayItem(
    animatedContentScope: AnimatedContentScope,
    weekNumber: Int,
    dayPlan: DayPlanPresentationModel,
    modifier: Modifier = Modifier,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val day = dayPlan.day
    val dayNumber = day.number

    ListItem(
        modifier = modifier
            .clickable {
                onEvent(
                    ReadingPlanUiEvent.OnDayClick(
                        dayNumber = dayNumber,
                        weekNumber = weekNumber,
                    ),
                )
            },
        leadingContent = {
            AnimatedPlannedReadDateComponent(
                dayPlan = dayPlan,
                animatedContentScope = animatedContentScope,
                weekNumber = weekNumber,
                dayNumber = dayNumber,
            )
        },
        headlineContent = {
            DayItemTexts(
                animatedContentScope = animatedContentScope,
                day = day,
                weekNumber = weekNumber,
            )
        },
        trailingContent = {
            Checkbox(
                checked = day.isRead,
                onCheckedChange = {
                    onEvent(
                        ReadingPlanUiEvent.OnDayReadClick(
                            dayNumber = dayNumber,
                            weekNumber = weekNumber,
                        ),
                    )
                },
            )
        },
    )
}
