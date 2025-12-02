package com.quare.bibleplanner.feature.day.presentation.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.mark_as_read
import bibleplanner.feature.day.generated.resources.mark_as_unread
import com.quare.bibleplanner.feature.day.presentation.component.ChangeReadStatusButton
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.passageList
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoadedDayContent(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        val isDayRead = uiState.day.isRead
        centeredContentItem(maxContentWidth) {
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

        passageList(
            passages = uiState.day.passages,
            chapterReadStatus = uiState.chapterReadStatus,
            onChapterToggle = { passageIndex, chapterIndex ->
                onEvent(DayUiEvent.OnChapterToggle(passageIndex, chapterIndex))
            },
            maxContentWidth = maxContentWidth,
        )

        centeredContentItem(maxContentWidth) {
            DayReadSection(
                modifier = Modifier.padding(horizontal = 8.dp),
                isRead = isDayRead,
                formattedReadDate = uiState.formattedReadDate,
                onEvent = onEvent,
            )
        }
    }
}

private fun LazyListScope.centeredContentItem(
    maxContentWidth: Dp,
    content: @Composable () -> Unit,
) {
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.width(maxContentWidth)) {
                content()
            }
        }
    }
}
