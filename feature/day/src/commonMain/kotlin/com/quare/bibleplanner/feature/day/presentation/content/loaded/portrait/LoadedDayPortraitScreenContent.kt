package com.quare.bibleplanner.feature.day.presentation.content.loaded.portrait

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
import com.quare.bibleplanner.feature.day.presentation.component.ChangeReadStatusButton
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.notes.NotesSection
import com.quare.bibleplanner.feature.day.presentation.component.portraitPassageList
import com.quare.bibleplanner.feature.day.presentation.content.loaded.PlannedReadDateComponent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun LoadedDayPortraitScreenContent(
    day: DayModel,
    onEvent: (DayUiEvent) -> Unit,
    uiState: DayUiState.Loaded,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val isDayRead = day.isRead
        item {
            ChangeReadStatusButton(
                isDayRead = isDayRead,
                buttonModifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                onClick = {
                    onEvent(DayUiEvent.OnDayReadToggle)
                },
            )
        }

        portraitPassageList(
            passages = day.passages,
            chapterReadStatus = uiState.chapterReadStatus,
            onChapterToggle = { passageIndex, chapterIndex ->
                onEvent(DayUiEvent.OnChapterToggle(passageIndex, chapterIndex))
            },
        )

        day.plannedReadDate?.let { plannedReadDate ->
            with(sharedTransitionScope) {
                item {
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
                isRead = isDayRead,
                formattedReadDate = uiState.formattedReadDate,
                onEvent = onEvent,
            )
        }

        item {
            NotesSection(
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                notesText = uiState.notesText,
                shouldClearFocus = uiState.shouldClearNotesFocus,
                onEvent = onEvent,
            )
        }
    }
}
