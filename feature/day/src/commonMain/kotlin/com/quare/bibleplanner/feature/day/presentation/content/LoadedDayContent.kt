package com.quare.bibleplanner.feature.day.presentation.content

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

private const val BREAKPOINT_WIDTH = 600

@Composable
internal fun LoadedDayContent(
    modifier: Modifier,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
    maxContentWidth: Dp? = null,
) {
    BoxWithConstraints(modifier = modifier) {
        val isLargeScreenWidth = maxWidth >= BREAKPOINT_WIDTH.dp
        val contentWidth = maxContentWidth ?: maxWidth

        if (isLargeScreenWidth) {
            DayLargeWidthContent(uiState, onEvent, contentWidth)
        } else {
            DayCompactWidthContent(uiState, onEvent, contentWidth)
        }
    }
}
