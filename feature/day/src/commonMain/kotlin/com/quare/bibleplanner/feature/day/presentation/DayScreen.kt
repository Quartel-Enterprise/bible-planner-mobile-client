package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.content.DayContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@Composable
internal fun DayScreen(
    uiState: DayUiState,
    onEvent: (DayUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            DayTopBar(
                uiState = uiState,
                onBackClick = { onEvent(DayUiEvent.OnBackClick) },
            )
        },
    ) { paddingValues ->
        DayContent(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        )
    }
}
