package com.quare.bibleplanner.android.feature.chat

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatMessageUiModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiState
import com.quare.bibleplanner.ui.utils.ReserveBottomOverlayHeightEffect
import kotlinx.coroutines.flow.Flow

private const val WELCOME_KEY = "welcome"
private const val PENDING_QUESTION_KEY = "pending_question"
private const val THINKING_KEY = "thinking"
private const val FAILURE_KEY = "failure"
private const val SUGGESTIONS_KEY = "suggestions"
private val sidebarLayoutMinWidth = 840.dp
private val headerElevation = 4.dp
private val sparkleSize = 22.dp
private val chatContentMaxWidth = 720.dp

@Composable
internal fun ChatContent(
    uiState: ChatUiState,
    scrollToBottomRequests: Flow<Unit>,
    onEvent: (ChatUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val listState = rememberLazyListState()
    var composerHeightPx by remember { mutableFloatStateOf(0f) }
    ReserveBottomOverlayHeightEffect { composerHeightPx }
    ChatScrollEffect(
        requests = scrollToBottomRequests,
        listState = listState,
        hasThread = uiState.messages.isNotEmpty(),
        isAnswering = uiState.isAnswering,
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= sidebarLayoutMinWidth
        if (isWide) {
            // Why: the sidebar replaces the drawer, which would otherwise reopen by itself once the window narrows.
            LaunchedEffect(uiState.history.isOpen) {
                if (uiState.history.isOpen) onEvent(ChatUiEvent.OnHistoryDismiss)
            }
            Row(modifier = Modifier.fillMaxSize()) {
                ChatHistorySidebar(
                    history = uiState.history,
                    onEvent = onEvent,
                    onNavigateBack = onNavigateBack,
                )
                VerticalDivider()
                ChatThreadScaffold(
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
            ChatThreadScaffold(
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
private fun ChatThreadScaffold(
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
        contentWindowInsets = if (isWide) {
            WindowInsets.safeDrawing.only(WindowInsetsSides.End + WindowInsetsSides.Vertical)
        } else {
            ScaffoldDefaults.contentWindowInsets
        },
        topBar = {
            val isScrolled by remember { derivedStateOf { listState.canScrollBackward } }
            val elevation by animateDpAsState(if (isScrolled) headerElevation else 0.dp)
            Surface(shadowElevation = elevation) {
                Column {
                    ChatTopBar(
                        contextLabel = uiState.contextLabel,
                        isWide = isWide,
                        onNavigateBack = onNavigateBack,
                        onEvent = onEvent,
                    )
                    if (!isWide) {
                        ChatContextPill(
                            contextLabel = uiState.contextLabel,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .widthIn(max = chatContentMaxWidth)
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp,
                                ),
                        )
                    }
                    if (isWide) HorizontalDivider()
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
        ) {
            ChatMessageList(
                uiState = uiState,
                listState = listState,
                onEvent = onEvent,
                modifier = Modifier.weight(1f),
            )
            if (isWide) HorizontalDivider()
            ChatComposerColumn(
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
                    text = stringResource(R.string.chat_title),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            if (!isWide) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.chat_back),
                    )
                }
            }
        },
        actions = {
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
                        text = stringResource(R.string.chat_new),
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
                IconButton(onClick = { onEvent(ChatUiEvent.OnNewConversationClick) }) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = stringResource(R.string.chat_new_conversation),
                    )
                }
                IconButton(onClick = { onEvent(ChatUiEvent.OnHistoryClick) }) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = stringResource(R.string.chat_conversations),
                    )
                }
            }
        },
    )
}

@Composable
private fun ChatMessageList(
    uiState: ChatUiState,
    listState: LazyListState,
    onEvent: (ChatUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val welcomeText = uiState.contextLabel
        ?.let { stringResource(R.string.chat_welcome_with_context, it) }
        ?: stringResource(R.string.chat_welcome_generic)
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
                    text = welcomeText,
                    isFromUser = false,
                    isStreaming = false,
                    isFailed = false,
                ),
                modifier = Modifier.centeredContent(),
            )
        }
        items(
            items = uiState.messages,
            key = { message -> message.id },
        ) { message ->
            if (message.isFailed) {
                ChatFailureCard(
                    failure = ChatSendFailureModel.Generic,
                    cooldownSeconds = uiState.cooldownSeconds,
                    onRetryClick = { onEvent(ChatUiEvent.OnRetryClick) },
                    modifier = Modifier.centeredContent(),
                )
            } else {
                ChatMessageBubble(
                    message = message,
                    modifier = Modifier.centeredContent(),
                )
            }
        }
        uiState.visiblePendingQuestion?.let { question ->
            item(key = PENDING_QUESTION_KEY) {
                ChatMessageBubble(
                    message = ChatMessageUiModel(
                        id = PENDING_QUESTION_KEY,
                        text = question,
                        isFromUser = true,
                        isStreaming = false,
                        isFailed = false,
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
                ChatSuggestionChipColumn(
                    suggestions = uiState.suggestions,
                    onSuggestionClick = { suggestion -> onEvent(ChatUiEvent.OnSuggestionClick(suggestion)) },
                    modifier = Modifier.centeredContent(),
                )
            }
        }
    }
}

@Composable
private fun ChatComposerColumn(
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
                modifier = Modifier.widthIn(max = chatContentMaxWidth),
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
            modifier = Modifier.widthIn(max = chatContentMaxWidth),
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

private fun Modifier.centeredContent(): Modifier = this
    .widthIn(max = chatContentMaxWidth)
    .fillMaxWidth()
