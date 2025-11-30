package com.quare.bibleplanner.feature.day.presentation.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.day.presentation.component.DayProgress
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.passageList
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@Composable
internal fun DayCompactWidthContent(
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            DayProgress(
                passages = uiState.day.passages,
                books = uiState.books,
            )
        }

        passageList(
            passages = uiState.day.passages,
            books = uiState.books,
            onChapterToggle = { passageIndex, chapterIndex ->
                onEvent(DayUiEvent.OnChapterToggle(passageIndex, chapterIndex))
            },
        )

        item {
            DayReadSection(
                isRead = uiState.day.isRead,
                formattedReadDate = uiState.formattedReadDate,
                onEvent = onEvent,
            )
        }
    }
}
