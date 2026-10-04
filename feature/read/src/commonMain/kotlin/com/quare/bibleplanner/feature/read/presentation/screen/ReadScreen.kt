package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.DayCompletionBannerSlot
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout

@Composable
internal fun ReadScreen(
    platform: Platform,
    state: ReadUiState,
    listening: ReadListeningUiState,
    onEvent: (ReadUiEvent) -> Unit,
    onListeningEvent: (ReadListeningUiEvent) -> Unit,
    dayCompletionBanner: DayCompletionBannerSlot,
) {
    // Why: there is nothing to read aloud until the chapter text is downloaded.
    val shownListening = if (state.content is ReadContentUiState.Success) {
        listening
    } else {
        listening.copy(
            isAvailable = false,
        )
    }
    if (LocalIsWideLayout.current) {
        ReadWideScreen(
            platform = platform,
            state = state,
            listening = shownListening,
            onEvent = onEvent,
            onListeningEvent = onListeningEvent,
            dayCompletionBanner = dayCompletionBanner,
        )
    } else {
        ReadNarrowScreen(
            platform = platform,
            state = state,
            listening = shownListening,
            onEvent = onEvent,
            onListeningEvent = onListeningEvent,
            dayCompletionBanner = dayCompletionBanner,
        )
    }
}
