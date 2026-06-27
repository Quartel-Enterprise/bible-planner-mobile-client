package com.quare.bibleplanner.feature.readingplan.presentation.component.week

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.component.week.day.AnimatedDaysList
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekGroup
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun WeekPlanItem(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    weekPresentation: WeekPlanPresentationModel,
    flashTargetGlobalIndex: Int,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCurrent = weekPresentation.group == WeekGroup.Current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = if (isCurrent) {
            BorderStroke(width = 1.5.dp, color = MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            with(sharedTransitionScope) {
                WeekRow(
                    onEvent = onEvent,
                    weekPresentation = weekPresentation,
                    animatedContentScope = animatedContentScope,
                )
                AnimatedDaysList(
                    weekPresentation = weekPresentation,
                    flashTargetGlobalIndex = flashTargetGlobalIndex,
                    onEvent = onEvent,
                    animatedContentScope = animatedContentScope,
                )
            }
        }
    }
}
