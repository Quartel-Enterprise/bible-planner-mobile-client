package com.quare.bibleplanner.feature.day.presentation.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.day_week_title
import bibleplanner.feature.day.generated.resources.mark_as_read
import bibleplanner.feature.day.generated.resources.mark_as_unread
import com.quare.bibleplanner.feature.day.presentation.component.DayProgress
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.passageList
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoadedDayContent(
    modifier: Modifier = Modifier,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
    maxContentWidth: Dp,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            Text(
                text = stringResource(
                    Res.string.day_week_title,
                    uiState.day.number,
                    uiState.weekNumber,
                ),
            )
        }
        item {
            VerticalSpacer()
        }
        item {
            OutlinedButton(
                onClick = {
                    onEvent(
                        DayUiEvent.OnDayReadToggle(!uiState.day.isRead)
                    )
                },
            ) {
                Text(
                    text = stringResource(if (uiState.day.isRead) Res.string.mark_as_unread else Res.string.mark_as_read),
                )
            }
        }

        // Show the read section (completed date, etc.) when the day is marked as read
        item {
            DayReadSection(
                isRead = uiState.day.isRead,
                formattedReadDate = uiState.formattedReadDate,
                onEvent = onEvent,
            )
        }

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
            maxContentWidth = maxContentWidth,
        )
    }
}
