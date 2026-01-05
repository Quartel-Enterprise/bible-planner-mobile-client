package com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.utils.orFalse
import com.quare.bibleplanner.feature.day.presentation.component.ChangeReadStatusButton
import com.quare.bibleplanner.feature.day.presentation.component.ChapterItemComponent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

import androidx.compose.foundation.layout.Column

@Composable
internal fun LoadedDayLandscapeScreenLeftContent(
    day: DayModel,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
) {
    val isDayRead = day.isRead
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
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

        day.passages.forEachIndexed { passageIndex, passage ->
            val onToggle = { onEvent(DayUiEvent.OnChapterToggle(passageIndex, -1)) }
            val commonModifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
                .padding(vertical = 8.dp)
            if (passage.chapters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle() },
                ) {
                    ChapterItemComponent(
                        modifier = commonModifier,
                        bookName = passage.bookId.getBookName(),
                        chapterPlanModel = null,
                        isRead = passage.isRead,
                        onToggle = onToggle,
                    )
                }
                if (passageIndex < day.passages.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            } else {
                // Show each chapter as a separate item
                passage.chapters.forEachIndexed { chapterIndex, chapter ->
                    val chapterToggle = { onEvent(DayUiEvent.OnChapterToggle(passageIndex, chapterIndex)) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { chapterToggle() },
                    ) {
                        ChapterItemComponent(
                            modifier = commonModifier,
                            bookName = passage.bookId.getBookName(),
                            chapterPlanModel = chapter,
                            isRead = uiState.chapterReadStatus[passageIndex to chapterIndex].orFalse(),
                            onToggle = chapterToggle,
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
