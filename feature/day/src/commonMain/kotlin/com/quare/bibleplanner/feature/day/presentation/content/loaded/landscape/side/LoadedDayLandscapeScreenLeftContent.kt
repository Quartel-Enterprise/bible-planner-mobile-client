package com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.day.presentation.component.AskAiButton
import com.quare.bibleplanner.feature.day.presentation.component.ChangeReadStatusButton
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.PassageRowsColumn
import com.quare.bibleplanner.feature.day.presentation.component.notes.NotesSection
import com.quare.bibleplanner.feature.day.presentation.content.loaded.PlannedReadDateComponent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@Composable
internal fun LoadedDayLandscapeScreenLeftContent(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    day: DayModel,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PassagesCard(
            day = day,
            uiState = uiState,
            onEvent = onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
        ChangeReadStatusButton(
            isDayRead = day.isRead,
            buttonModifier = Modifier.fillMaxWidth(),
            onClick = { onEvent(DayUiEvent.OnDayReadToggle) },
        )
        AskAiButton(
            onClick = { onEvent(DayUiEvent.OnAskAiClick) },
            modifier = Modifier.fillMaxWidth(),
        )
        day.plannedReadDate?.let { plannedReadDate ->
            with(sharedTransitionScope) {
                PlannedReadDateComponent(
                    plannedReadDate = plannedReadDate,
                    animatedContentScope = animatedContentScope,
                    weekNumber = uiState.weekNumber,
                    dayNumber = day.number,
                )
            }
        }
        DayReadSection(
            isRead = day.isRead,
            formattedReadDate = uiState.formattedReadDate,
            onEvent = onEvent,
        )
        NotesSection(
            notesText = day.notes.orEmpty(),
            onEvent = onEvent,
        )
    }
}

@Composable
private fun PassagesCard(
    day: DayModel,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
    ) {
        PassageRowsColumn(
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            passages = day.passages,
            chapterReadStatus = uiState.chapterReadStatus,
            itemStartPadding = 8.dp,
            onEvent = onEvent,
        )
    }
}
