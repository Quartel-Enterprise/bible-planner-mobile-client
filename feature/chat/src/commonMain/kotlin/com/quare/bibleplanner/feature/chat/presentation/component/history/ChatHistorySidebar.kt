package com.quare.bibleplanner.feature.chat.presentation.component.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.chat.presentation.model.ChatHistoryUiState
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent

private val sidebarWidth = 304.dp
private val listPadding = PaddingValues(
    start = 12.dp,
    end = 12.dp,
    bottom = 24.dp,
)

/**
 * The conversation list as a permanent column, for windows wide enough to show the history without
 * taking the thread away. It spans the full height beside the chat, so the way out of the screen
 * lives here rather than in the top bar.
 */
@Composable
internal fun ChatHistorySidebar(
    history: ChatHistoryUiState,
    onEvent: (ChatUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        ChatHistoryPane(
            history = history,
            onEvent = onEvent,
            onNavigateBack = onNavigateBack,
            onDismiss = null,
            contentPadding = listPadding,
        )
    }
}
