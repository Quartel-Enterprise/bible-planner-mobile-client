package com.quare.bibleplanner.feature.chat.presentation.component.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
        Box(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
            ),
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
}
