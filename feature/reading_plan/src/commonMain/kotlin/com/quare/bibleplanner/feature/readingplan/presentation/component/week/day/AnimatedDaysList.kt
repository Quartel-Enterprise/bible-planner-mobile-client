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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.AnimatedDaysList(
    animatedContentScope: AnimatedContentScope,
    weekPresentation: WeekPlanPresentationModel,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    AnimatedVisibility(
        visible = weekPresentation.isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        weekPresentation.weekPlan.run {
            Column {
                days.forEach { day ->
                    DayItem(
                        animatedContentScope = animatedContentScope,
                        day = day,
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        onEvent = onEvent,
                        weekNumber = number,
                    )
                }
            }
        }
    }
}
