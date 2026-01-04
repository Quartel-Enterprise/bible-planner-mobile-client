package com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.notes.NotesSection
import com.quare.bibleplanner.feature.day.presentation.content.loaded.PlannedReadDateComponent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun LoadedDayLandscapeScreenRightContent(
    day: DayModel,
    uiState: DayUiState.Loaded,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onEvent: (DayUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        day.plannedReadDate?.let { plannedReadDate ->
            item {
                with(sharedTransitionScope) {
                    PlannedReadDateComponent(
                        modifier = Modifier.padding(8.dp),
                        plannedReadDate = plannedReadDate,
                        animatedContentScope = animatedContentScope,
                        weekNumber = uiState.weekNumber,
                        dayNumber = day.number,
                    )
                }
            }
        }

        item {
            DayReadSection(
                modifier = Modifier.padding(horizontal = 8.dp),
                isRead = day.isRead,
                formattedReadDate = uiState.formattedReadDate,
                onEvent = onEvent,
            )
        }

        item {
            NotesSection(
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                notesText = day.notes.orEmpty(),
                onEvent = onEvent,
            )
        }
    }
}
