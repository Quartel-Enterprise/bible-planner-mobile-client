package com.quare.bibleplanner.feature.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_context_chip
import bibleplanner.feature.chat.generated.resources.chat_conversations
import bibleplanner.feature.chat.generated.resources.chat_history
import bibleplanner.feature.chat.generated.resources.chat_new
import bibleplanner.feature.chat.generated.resources.chat_new_conversation
import bibleplanner.feature.chat.generated.resources.chat_title
import bibleplanner.feature.chat.generated.resources.chat_welcome_generic
import bibleplanner.feature.chat.generated.resources.chat_welcome_with_context
import com.quare.bibleplanner.feature.chat.presentation.component.ChatContextPill
import com.quare.bibleplanner.feature.chat.presentation.component.ChatFailureCard
import com.quare.bibleplanner.feature.chat.presentation.component.ChatInputBar
import com.quare.bibleplanner.feature.chat.presentation.component.ChatMessageBubble
import com.quare.bibleplanner.feature.chat.presentation.component.ChatSuggestionBar
import com.quare.bibleplanner.feature.chat.presentation.component.ChatSuggestionChips
import com.quare.bibleplanner.feature.chat.presentation.component.ChatThinkingIndicator
import com.quare.bibleplanner.feature.chat.presentation.component.history.ChatHistoryDrawer
import com.quare.bibleplanner.feature.chat.presentation.model.ChatMessageUiModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource

private const val WELCOME_KEY = "welcome"
private const val PENDING_QUESTION_KEY = "pending_question"
private const val THINKING_KEY = "thinking"
private const val FAILURE_KEY = "failure"
private const val SUGGESTIONS_KEY = "suggestions"
private val contentMaxWidth = 720.dp
private val sparkleSize = 22.dp

@Composable
internal fun ChatScreen(
    uiState: ChatUiState,
    scrollToBottomRequests: Flow<Unit>,
    onEvent: (ChatUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val isWide = LocalIsWideLayout.current
    val listState = rememberLazyListState()
    ScrollToBottomEffect(
        requests = scrollToBottomRequests,
        listState = listState,
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                ChatTopBar(
                    contextLabel = uiState.contextLabel,
                    isWide = isWide,
                    onNavigateBack = onNavigateBack,
                    onEvent = onEvent,
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
            ) {
                if (!isWide) {
                    ChatContextPill(
                        contextLabel = uiState.contextLabel,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .widthIn(max = contentMaxWidth)
                            .padding(horizontal = 12.dp),
                    )
                }
                ChatMessages(
                    uiState = uiState,
                    listState = listState,
                    onEvent = onEvent,
                    modifier = Modifier.weight(1f),
                )
                ChatComposer(
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        }
        ChatHistoryDrawer(
            history = uiState.history,
            onEvent = onEvent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    contextLabel: String?,
    isWide: Boolean,
    onNavigateBack: () -> Unit,
    onEvent: (ChatUiEvent) -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(sparkleSize),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(Res.string.chat_title),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        actions = {
            // On a wide window the bar has room to name its actions and to carry the reading
            // context itself, so the pill below the bar is dropped there.
            if (isWide) {
                TextButton(onClick = { onEvent(ChatUiEvent.OnHistoryClick) }) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(Res.string.chat_history),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                TextButton(onClick = { onEvent(ChatUiEvent.OnNewConversationClick) }) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(Res.string.chat_new),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                contextLabel?.let { label ->
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(Res.string.chat_context_chip, label)) },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                            )
                        },
                    )
                }
            } else {
                IconButton(onClick = { onEvent(ChatUiEvent.OnHistoryClick) }) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = stringResource(Res.string.chat_conversations),
                    )
                }
                IconButton(onClick = { onEvent(ChatUiEvent.OnNewConversationClick) }) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = stringResource(Res.string.chat_new_conversation),
                    )
                }
            }
        },
    )
}

@Composable
private fun ChatMessages(
    uiState: ChatUiState,
    listState: LazyListState,
    onEvent: (ChatUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = WELCOME_KEY) {
            ChatMessageBubble(
                message = ChatMessageUiModel(
                    id = WELCOME_KEY,
                    text = uiState.contextLabel
                        ?.let { stringResource(Res.string.chat_welcome_with_context, it) }
                        ?: stringResource(Res.string.chat_welcome_generic),
                    isFromUser = false,
                    isStreaming = false,
                ),
                modifier = Modifier.centeredContent(),
            )
        }
        items(
            items = uiState.messages,
            key = { message -> message.id },
        ) { message ->
            ChatMessageBubble(
                message = message,
                modifier = Modifier.centeredContent(),
            )
        }
        uiState.visiblePendingQuestion?.let { question ->
            item(key = PENDING_QUESTION_KEY) {
                ChatMessageBubble(
                    message = ChatMessageUiModel(
                        id = PENDING_QUESTION_KEY,
                        text = question,
                        isFromUser = true,
                        isStreaming = false,
                    ),
                    modifier = Modifier.centeredContent(),
                )
            }
        }
        if (uiState.isThinking) {
            item(key = THINKING_KEY) {
                ChatThinkingIndicator(modifier = Modifier.centeredContent())
            }
        }
        uiState.failure?.let { failure ->
            item(key = FAILURE_KEY) {
                ChatFailureCard(
                    failure = failure,
                    cooldownSeconds = uiState.cooldownSeconds,
                    onRetryClick = { onEvent(ChatUiEvent.OnRetryClick) },
                    modifier = Modifier.centeredContent(),
                )
            }
        }
        if (uiState.showInitialSuggestions) {
            item(key = SUGGESTIONS_KEY) {
                ChatSuggestionChips(
                    suggestions = uiState.suggestions,
                    onSuggestionClick = { suggestion -> onEvent(ChatUiEvent.OnSuggestionClick(suggestion)) },
                    modifier = Modifier.centeredContent(),
                )
            }
        }
    }
}

@Composable
private fun ChatComposer(
    uiState: ChatUiState,
    onEvent: (ChatUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (uiState.showSuggestionBar) {
            ChatSuggestionBar(
                suggestions = uiState.suggestions,
                isExpanded = uiState.isSuggestionBarExpanded,
                onToggle = { onEvent(ChatUiEvent.OnSuggestionBarToggle) },
                onSuggestionClick = { suggestion -> onEvent(ChatUiEvent.OnSuggestionClick(suggestion)) },
                modifier = Modifier.widthIn(max = contentMaxWidth),
            )
        }
        ChatInputBar(
            input = uiState.input,
            mode = uiState.inputMode,
            cooldownSeconds = uiState.cooldownSeconds,
            contextLabel = uiState.contextLabel,
            quota = uiState.quota,
            onInputChange = { text -> onEvent(ChatUiEvent.OnInputChanged(text)) },
            onSendClick = { onEvent(ChatUiEvent.OnSendClick) },
            onSubscribeClick = { onEvent(ChatUiEvent.OnSubscribeClick) },
            modifier = Modifier.widthIn(max = contentMaxWidth),
        )
        VerticalSpacer(4)
    }
}

private fun Modifier.centeredContent(): Modifier = this.widthIn(max = contentMaxWidth)

// New content always lands at the bottom of the thread, the way a chat is read.
@Composable
private fun ScrollToBottomEffect(
    requests: Flow<Unit>,
    listState: LazyListState,
) {
    LaunchedEffect(requests) {
        requests.collect {
            val lastIndex = listState.layoutInfo.totalItemsCount - 1
            if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
        }
    }
}
