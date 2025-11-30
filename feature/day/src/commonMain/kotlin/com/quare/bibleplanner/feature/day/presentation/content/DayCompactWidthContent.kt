package com.quare.bibleplanner.feature.day.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.feature.day.presentation.component.DayProgress
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.passageList
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@Composable
internal fun DayCompactWidthContent(
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
    maxContentWidth: Dp,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(maxContentWidth)) {
                    DayProgress(
                        passages = uiState.day.passages,
                        books = uiState.books,
                    )
                }
            }
        }

        passageList(
            passages = uiState.day.passages,
            books = uiState.books,
            onChapterToggle = { passageIndex, chapterIndex ->
                onEvent(DayUiEvent.OnChapterToggle(passageIndex, chapterIndex))
            },
            maxContentWidth = maxContentWidth,
        )

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(maxContentWidth)) {
                    DayReadSection(
                        isRead = uiState.day.isRead,
                        formattedReadDate = uiState.formattedReadDate,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}
