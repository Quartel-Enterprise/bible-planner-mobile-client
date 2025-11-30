package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.content.DayContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

private const val MAX_CONTENT_WIDTH = 600

@Composable
internal fun DayScreen(
    uiState: DayUiState,
    onEvent: (DayUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            DayTopBar(
                uiState = uiState,
                onEvent = onEvent,
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val constrainedWidth = maxWidth.coerceAtMost(MAX_CONTENT_WIDTH.dp)
            DayContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                maxContentWidth = constrainedWidth,
            )
        }
    }
}
