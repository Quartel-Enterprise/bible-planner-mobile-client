package com.quare.bibleplanner.android.feature.read

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout

@Composable
internal fun ReadContent(
    state: ReadUiState,
    listening: ReadListeningUiState,
    onEvent: (ReadUiEvent) -> Unit,
    onListeningEvent: (ReadListeningUiEvent) -> Unit,
) {
    // Why: there is nothing to read aloud until the chapter text is downloaded.
    val shownListening = if (state.content is ReadContentUiState.Success) {
        listening
    } else {
        listening.copy(isAvailable = false)
    }
    if (LocalIsWideLayout.current) {
        ReadWideScreen(
            state = state,
            listening = shownListening,
            onEvent = onEvent,
            onListeningEvent = onListeningEvent,
        )
    } else {
        ReadNarrowScreen(
            state = state,
            listening = shownListening,
            onEvent = onEvent,
            onListeningEvent = onListeningEvent,
        )
    }
}
