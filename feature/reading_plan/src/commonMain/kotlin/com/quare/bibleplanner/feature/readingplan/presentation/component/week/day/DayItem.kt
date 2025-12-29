package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.DayItem(
    animatedContentScope: AnimatedContentScope,
    weekNumber: Int,
    day: DayModel,
    modifier: Modifier = Modifier,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val dayNumber = day.number
    Column(
        modifier = modifier
            .clickable {
                onEvent(
                    ReadingPlanUiEvent.OnDayClick(
                        dayNumber = dayNumber,
                        weekNumber = weekNumber,
                    ),
                )
            }.padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AnimatedContent(
                    targetState = day.plannedReadDate,
                ) { plannedReadDate ->
                    if (plannedReadDate != null) {
                        PlannedReadDateComponent(
                            modifier = Modifier,
                            plannedReadDate = plannedReadDate,
                            isRead = day.isRead,
                        )
                    }
                }
                DayItemTexts(
                    animatedContentScope = animatedContentScope,
                    day = day,
                    weekNumber = weekNumber,
                )
            }
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
        }
    }
}
