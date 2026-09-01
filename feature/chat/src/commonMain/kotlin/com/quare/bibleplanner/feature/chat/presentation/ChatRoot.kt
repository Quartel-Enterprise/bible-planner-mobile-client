package com.quare.bibleplanner.feature.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiAction
import com.quare.bibleplanner.feature.chat.presentation.viewmodel.ChatViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.chat() {
    entry<ChatNavRoute> { route ->
        ChatRootContent(route = route)
    }
}

@Composable
private fun ChatRootContent(route: ChatNavRoute) {
    val viewModel = koinViewModel<ChatViewModel> { parametersOf(route) }
    val navigator = koinInject<Navigator>()
    val uiState by viewModel.uiState.collectAsState()
    val scrollToBottomRequests = remember { Channel<Unit>(Channel.CONFLATED) }

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            ChatUiAction.ScrollToBottom -> scrollToBottomRequests.send(Unit)
        }
    }

    ChatScreen(
        uiState = uiState,
        scrollToBottomRequests = remember(scrollToBottomRequests) { scrollToBottomRequests.receiveAsFlow() },
        onEvent = viewModel::onEvent,
        onNavigateBack = navigator::navigateBack,
    )
}
