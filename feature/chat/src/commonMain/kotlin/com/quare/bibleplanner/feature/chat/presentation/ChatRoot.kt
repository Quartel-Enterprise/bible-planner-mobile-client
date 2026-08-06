package com.quare.bibleplanner.feature.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiAction
import com.quare.bibleplanner.feature.chat.presentation.viewmodel.ChatViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.chat(
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<ChatNavRoute> { route ->
        ChatRootContent(
            route = route,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
        )
    }
}

@Composable
private fun ChatRootContent(
    route: ChatNavRoute,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<ChatViewModel> { parametersOf(route) }
    val uiState by viewModel.uiState.collectAsState()
    val scrollToBottomRequests = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is ChatUiAction.NavigateToRoute -> onNavigate(action.route)
            ChatUiAction.ScrollToBottom -> scrollToBottomRequests.tryEmit(Unit)
        }
    }

    ChatScreen(
        uiState = uiState,
        scrollToBottomRequests = scrollToBottomRequests,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
    )
}
