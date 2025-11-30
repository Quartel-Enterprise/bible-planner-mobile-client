package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.content.DayContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayBottomSheet(
    uiState: DayUiState,
    onEvent: (DayUiEvent) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onEvent(DayUiEvent.OnBackClick) },
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            DayContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            )
        }
    }
}
