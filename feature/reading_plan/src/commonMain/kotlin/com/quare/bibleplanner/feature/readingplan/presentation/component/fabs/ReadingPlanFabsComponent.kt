package com.quare.bibleplanner.feature.readingplan.presentation.component.fabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState

@Composable
internal fun ReadingPlanFabsComponent(
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScrollToUpFab(
            isVisible = uiState.isScrolledDown,
            onClick = {
                onEvent(ReadingPlanUiEvent.OnScrollToTopClick)
            },
        )
        if (uiState is ReadingPlanUiState.Loaded) {
            GoToActiveFab(
                isVisible = !uiState.isActiveRowVisible,
                planMode = uiState.planStatus.mode,
                onClick = {
                    onEvent(ReadingPlanUiEvent.OnGoToActiveRowClick)
                },
            )
        }
    }
}
