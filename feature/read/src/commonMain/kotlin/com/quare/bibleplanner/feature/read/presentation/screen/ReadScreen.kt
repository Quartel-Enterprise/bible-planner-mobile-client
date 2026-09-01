package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout

@Composable
internal fun ReadScreen(
    platform: Platform,
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
) {
    if (LocalIsWideLayout.current) {
        ReadWideScreen(
            platform = platform,
            state = state,
            onEvent = onEvent,
        )
    } else {
        ReadNarrowScreen(
            platform = platform,
            state = state,
            onEvent = onEvent,
        )
    }
}
