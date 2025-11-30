package com.quare.bibleplanner.feature.day.presentation.content

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@Composable
internal fun BoxWithConstraintsScope.DayContent(
    uiState: DayUiState,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is DayUiState.Loading -> LoadingDayContent(modifier)

        is DayUiState.Loaded -> LoadedDayContent(
            modifier = modifier,
            uiState = uiState,
            onEvent = onEvent,
            maxContentWidth = maxWidth.coerceAtMost(600.dp)
        )
    }
}
