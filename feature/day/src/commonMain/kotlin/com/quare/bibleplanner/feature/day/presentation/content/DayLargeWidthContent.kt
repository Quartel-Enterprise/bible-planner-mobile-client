package com.quare.bibleplanner.feature.day.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.component.DayProgress
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.passageList
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@Composable
internal fun DayLargeWidthContent(
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 16.dp),
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
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            DayReadSection(
                isRead = uiState.day.isRead,
                formattedReadDate = uiState.formattedReadDate,
                onEvent = onEvent,
            )
        }
    }
}
