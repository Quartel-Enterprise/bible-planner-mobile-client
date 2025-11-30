package com.quare.bibleplanner.feature.day.presentation.content

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

private const val BREAKPOINT_WIDTH = 600

@Composable
internal fun LoadedDayContent(
    modifier: Modifier,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val isLargeScreenWidth = maxWidth >= BREAKPOINT_WIDTH.dp
        if (isLargeScreenWidth) {
            DayLargeWidthContent(uiState, onEvent)
        } else {
            DayCompactWidthContent(uiState, onEvent)
        }
    }
}
