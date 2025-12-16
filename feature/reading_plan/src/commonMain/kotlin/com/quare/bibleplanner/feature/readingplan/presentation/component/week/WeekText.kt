package com.quare.bibleplanner.feature.readingplan.presentation.component.week

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.WeekText(
    animatedContentScope: AnimatedContentScope,
    weekNumber: Int,
    readDaysCount: Int,
    totalDays: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        WeekNumberComponent(
            weekNumber = weekNumber,
            animatedContentScope = animatedContentScope,
        )
        WeekProgressSeparatorComponent(
            weekNumber = weekNumber,
            animatedContentScope = animatedContentScope,
        )
        WeekProgressLabel(
            readDaysCount = readDaysCount,
            totalDays = totalDays,
        )
    }
}
