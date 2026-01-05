package com.quare.bibleplanner.feature.day.presentation.content.loaded.portrait

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.centeredItem
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.day.presentation.component.ChangeReadStatusButton
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.notes.NotesSection
import com.quare.bibleplanner.feature.day.presentation.component.portraitPassageList
import com.quare.bibleplanner.feature.day.presentation.content.loaded.PlannedReadDateComponent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@OptIn(ExperimentalSharedTransitionApi::class)
internal fun LazyListScope.loadedDayPortraitScreenContent(
    contentMaxWidth: Dp,
    day: DayModel,
    onEvent: (DayUiEvent) -> Unit,
    uiState: DayUiState.Loaded,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val isDayRead = day.isRead
    centeredItem(contentMaxWidth) {
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
        contentMaxWidth = contentMaxWidth,
        passages = day.passages,
        chapterReadStatus = uiState.chapterReadStatus,
        onChapterToggle = { passageIndex, chapterIndex ->
            onEvent(DayUiEvent.OnChapterToggle(passageIndex, chapterIndex))
        },
    )

    day.plannedReadDate?.let { plannedReadDate ->
        with(sharedTransitionScope) {
            centeredItem(contentMaxWidth) {
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

    centeredItem(contentMaxWidth) {
        DayReadSection(
            modifier = Modifier.padding(horizontal = 8.dp),
            isRead = isDayRead,
            formattedReadDate = uiState.formattedReadDate,
            onEvent = onEvent,
        )
    }

    centeredItem(contentMaxWidth) {
        NotesSection(
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            notesText = day.notes.orEmpty(),
            onEvent = onEvent,
        )
    }
}
