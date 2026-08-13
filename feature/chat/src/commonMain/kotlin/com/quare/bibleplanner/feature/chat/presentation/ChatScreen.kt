package com.quare.bibleplanner.feature.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_back
import bibleplanner.feature.chat.generated.resources.chat_conversations
import bibleplanner.feature.chat.generated.resources.chat_new
import bibleplanner.feature.chat.generated.resources.chat_new_conversation
import bibleplanner.feature.chat.generated.resources.chat_title
import bibleplanner.feature.chat.generated.resources.chat_welcome_generic
import bibleplanner.feature.chat.generated.resources.chat_welcome_with_context
import com.quare.bibleplanner.feature.chat.presentation.component.ChatContextChip
import com.quare.bibleplanner.feature.chat.presentation.component.ChatContextPill
import com.quare.bibleplanner.feature.chat.presentation.component.ChatFailureCard
import com.quare.bibleplanner.feature.chat.presentation.component.ChatInputBar
import com.quare.bibleplanner.feature.chat.presentation.component.ChatMessageBubble
import com.quare.bibleplanner.feature.chat.presentation.component.ChatSuggestionBar
import com.quare.bibleplanner.feature.chat.presentation.component.ChatSuggestionChips
import com.quare.bibleplanner.feature.chat.presentation.component.ChatThinkingIndicator
import com.quare.bibleplanner.feature.chat.presentation.component.history.ChatHistoryDrawer
import com.quare.bibleplanner.feature.chat.presentation.component.history.ChatHistorySidebar
import com.quare.bibleplanner.feature.chat.presentation.model.ChatMessageUiModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.ReserveBottomOverlayHeight
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource

private const val WELCOME_KEY = "welcome"
private const val PENDING_QUESTION_KEY = "pending_question"
private const val THINKING_KEY = "thinking"
private const val FAILURE_KEY = "failure"
private const val SUGGESTIONS_KEY = "suggestions"
private val contentMaxWidth = 720.dp
private val sidebarLayoutMinWidth = 840.dp
private val sparkleSize = 22.dp

@Composable
internal fun ChatScreen(
    uiState: ChatUiState,
    scrollToBottomRequests: Flow<Unit>,
    onEvent: (ChatUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val listState = rememberLazyListState()
    var composerHeightPx by remember { mutableFloatStateOf(0f) }
    // The composer owns the bottom edge here, so the app's snackbar has to clear it.
    ReserveBottomOverlayHeight { composerHeightPx }
    ScrollToBottomEffect(
        requests = scrollToBottomRequests,
        listState = listState,
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Measured here rather than taken from the window: the sidebar only earns its place if what
        // is left over still fits a thread and a top bar that names its actions. Just past the
        // window's own wide mark it does not, and the title was wrapping onto three lines.
        val isWide = maxWidth >= sidebarLayoutMinWidth
        // Wide enough for both at once: the history stops being a place you go to and becomes a
        // column you read from, which is why the way back out of the chat moves into it.
        if (isWide) {
            Row(modifier = Modifier.fillMaxSize()) {
                ChatHistorySidebar(
                    history = uiState.history,
                    onEvent = onEvent,
                    onNavigateBack = onNavigateBack,
                )
                VerticalDivider()
                ChatThread(
                    uiState = uiState,
                    listState = listState,
                    isWide = true,
                    onNavigateBack = onNavigateBack,
                    onEvent = onEvent,
                    onComposerHeightChange = { height -> composerHeightPx = height },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            ChatThread(
                uiState = uiState,
                listState = listState,
                isWide = false,
                onNavigateBack = onNavigateBack,
                onEvent = onEvent,
                onComposerHeightChange = { height -> composerHeightPx = height },
                modifier = Modifier.fillMaxSize(),
            )
            ChatHistoryDrawer(
                history = uiState.history,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun ChatThread(
    uiState: ChatUiState,
    listState: LazyListState,
    isWide: Boolean,
    onNavigateBack: () -> Unit,
    onEvent: (ChatUiEvent) -> Unit,
    onComposerHeightChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                ChatTopBar(
                    contextLabel = uiState.contextLabel,
                    isWide = isWide,
                    onNavigateBack = onNavigateBack,
                    onEvent = onEvent,
                )
                // The bar carries the context on a wide window, so it needs a line to close it off:
                // without one it reads as the first thing in the thread rather than as its header.
                if (isWide) HorizontalDivider()
            }
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
            // The wide thread has room to spare below it, so the composer needs a line of its own
            // to read as the floor of the conversation rather than as the last thing said in it.
            if (isWide) HorizontalDivider()
            ChatComposer(
                uiState = uiState,
                onEvent = onEvent,
                onHeightChange = onComposerHeightChange,
            )
        }
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            // On a wide window the sidebar already carries the way back, and a second arrow in the
            // bar would only ask the reader which one leaves the screen.
            if (!isWide) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(Res.string.chat_back),
                    )
                }
            }
        },
        actions = {
            // The wide bar has room to name the action and to wear the reading context itself, so
            // the banner below the bar is dropped there — and the history needs no button at all,
            // being permanently on screen.
            if (isWide) {
                OutlinedButton(
                    onClick = { onEvent(ChatUiEvent.OnNewConversationClick) },
                    modifier = Modifier.padding(end = 8.dp),
                ) {
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
                    ChatContextChip(
                        contextLabel = label,
                        modifier = Modifier.padding(end = 16.dp),
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
        horizontalAlignment = Alignment.CenterHorizontally,
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
    onHeightChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size -> onHeightChange(size.height.toFloat()) }
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

// Caps the column and fills it: without the fill, anything narrower than the thread — the thinking
// indicator, a failure card — would be centred by the list instead of starting where the answers do.
private fun Modifier.centeredContent(): Modifier = this
    .widthIn(max = contentMaxWidth)
    .fillMaxWidth()

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
