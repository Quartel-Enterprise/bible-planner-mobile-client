package com.quare.bibleplanner.android.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.quare.bibleplanner.feature.chat.presentation.model.ChatHistoryUiState
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent

private val drawerWidth = 320.dp
private val sidebarWidth = 304.dp
private val drawerListPadding = PaddingValues(
    start = 12.dp,
    end = 12.dp,
    bottom = 96.dp,
)
private val sidebarListPadding = PaddingValues(
    start = 12.dp,
    end = 12.dp,
    bottom = 24.dp,
)
private const val SCRIM_ALPHA = 0.45f

@Composable
internal fun BoxScope.ChatHistoryDrawer(
    history: ChatHistoryUiState,
    onEvent: (ChatUiEvent) -> Unit,
) {
    // Why: on Android, Back closes an open drawer before it leaves the screen.
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = history.isOpen,
        onBackCompleted = { onEvent(ChatUiEvent.OnHistoryDismiss) },
    )
    val interactionSource = remember { MutableInteractionSource() }
    AnimatedVisibility(
        visible = history.isOpen,
        modifier = Modifier.matchParentSize(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onEvent(ChatUiEvent.OnHistoryDismiss) },
                ),
        )
    }
    AnimatedVisibility(
        visible = history.isOpen,
        modifier = Modifier.align(Alignment.CenterEnd),
        enter = slideInHorizontally { width -> width },
        exit = slideOutHorizontally { width -> width },
    ) {
        Surface(
            modifier = Modifier
                .width(drawerWidth)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Box(modifier = Modifier.systemBarsPadding()) {
                ChatHistoryPane(
                    history = history,
                    onEvent = onEvent,
                    onNavigateBack = null,
                    onDismiss = { onEvent(ChatUiEvent.OnHistoryDismiss) },
                    contentPadding = drawerListPadding,
                )
                if (history.hasConversations) {
                    ExtendedFloatingActionButton(
                        onClick = { onEvent(ChatUiEvent.OnNewConversationClick) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                            )
                        },
                        text = { Text(stringResource(R.string.chat_new_conversation)) },
                    )
                }
            }
        }
    }
}

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
                contentPadding = sidebarListPadding,
            )
        }
    }
}
