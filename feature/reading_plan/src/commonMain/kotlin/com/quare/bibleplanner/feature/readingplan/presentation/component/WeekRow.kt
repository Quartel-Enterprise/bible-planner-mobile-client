package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel
import com.quare.bibleplanner.ui.component.icon.ArrowRotationIcon

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.WeekRow(
    animatedContentScope: AnimatedContentScope,
    weekPresentation: WeekPlanPresentationModel,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val weekNumber = weekPresentation.weekPlan.number
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    onEvent(ReadingPlanUiEvent.OnWeekExpandClick(weekNumber))
                },
            ).padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeekText(
            weekNumber = weekNumber,
            readDaysCount = weekPresentation.readDaysCount,
            totalDays = weekPresentation.totalDays,
            animatedContentScope = animatedContentScope,
        )
        ArrowRotationIcon(
            modifier = Modifier.size(24.dp),
            isUp = weekPresentation.isExpanded,
        )
    }
}
