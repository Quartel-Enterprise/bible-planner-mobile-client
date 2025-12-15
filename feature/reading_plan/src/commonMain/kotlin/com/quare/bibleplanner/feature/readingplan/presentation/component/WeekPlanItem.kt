package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun WeekPlanItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    weekPresentation: WeekPlanPresentationModel,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        with(sharedTransitionScope) {
            WeekRow(
                onEvent = onEvent,
                weekPresentation = weekPresentation,
                animatedContentScope = animatedContentScope,
            )
            AnimatedDaysList(
                weekPresentation = weekPresentation,
                onEvent = onEvent,
                animatedContentScope = animatedContentScope,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
    }
}
