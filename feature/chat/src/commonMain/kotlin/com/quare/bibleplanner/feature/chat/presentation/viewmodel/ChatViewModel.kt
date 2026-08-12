package com.quare.bibleplanner.feature.chat.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.ChatEntrySource
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.toDayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.chat.domain.coordinator.ChatStreamCoordinator
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import com.quare.bibleplanner.feature.chat.domain.usecase.ChatUseCases
import com.quare.bibleplanner.feature.chat.presentation.GetDefaultChatSuggestions
import com.quare.bibleplanner.feature.chat.presentation.mapper.ChatConversationGroupMapper
import com.quare.bibleplanner.feature.chat.presentation.mapper.ChatMessageUiMapper
import com.quare.bibleplanner.feature.chat.presentation.model.ChatHistoryUiState
import com.quare.bibleplanner.feature.chat.presentation.model.ChatInputMode
import com.quare.bibleplanner.feature.chat.presentation.model.ChatQuotaUiModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiAction
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ChatViewModel(
    route: ChatNavRoute,
    private val useCases: ChatUseCases,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val getDefaultSuggestions: GetDefaultChatSuggestions,
    private val coordinator: ChatStreamCoordinator,
    private val messageUiMapper: ChatMessageUiMapper,
    private val conversationGroupMapper: ChatConversationGroupMapper,
    trackEvent: TrackEvent,
) : TrackedViewModel<ChatUiEvent>(trackEvent) {
    private val _uiState: MutableStateFlow<ChatUiState> = MutableStateFlow(
        ChatUiState(
            contextLabel = null,
            messages = emptyList(),
            pendingQuestion = null,
            suggestions = emptyList(),
            isSuggestionBarExpanded = false,
            input = "",
            inputMode = ChatInputMode.ENABLED,
            cooldownSeconds = 0,
            quota = null,
            isThinking = false,
            isAnswering = false,
            failure = null,
            history = ChatHistoryUiState(
                isOpen = false,
                query = "",
                groups = emptyList(),
                hasConversations = false,
                expandedActionsId = null,
                renamingId = null,
                renameDraft = "",
                deletingId = null,
            ),
        ),
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<ChatUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<ChatUiAction> = _uiAction

    private val cooldownTick: Duration = 1.seconds

    private val dayRoute = route.toDayNavRoute()

    // Only the study screen hands over its own questions. From the day's button the chips are always
    // the same ones, instead of quietly depending on whether that study happens to be generated.
    private val usesStudyQuestions = route.source == ChatEntrySource.DAY_STUDY_QUESTIONS
    private val activeConversationId: MutableStateFlow<String?> = MutableStateFlow(null)
    private var conversations: List<ChatConversationModel> = emptyList()
    private var context: ChatContextModel? = null
    private var usedSuggestions: Set<String> = emptySet()

    // The day's own thread is claimed once, while the reader has not chosen otherwise. After they
    // start a fresh conversation or open another from the history, that choice stands: the day's
    // thread arriving late in a sync must not pull the screen back to it.
    private var isDayThreadClaimed = false
    private var cooldownJob: Job? = null
    private var isLoggedIn: Boolean = false

    init {
        loadContext()
        observeAuthentication()
        observeConversations()
        observeMessages()
        observeQuota()
        observeSend()
        syncRemoteChanges()
    }

    override fun handleEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.OnInputChanged -> _uiState.update { it.copy(input = event.text) }

            ChatUiEvent.OnSendClick -> send(_uiState.value.input)

            is ChatUiEvent.OnSuggestionClick -> onSuggestionClick(event.suggestion)

            ChatUiEvent.OnSuggestionBarToggle -> _uiState.update {
                it.copy(isSuggestionBarExpanded = !it.isSuggestionBarExpanded)
            }

            ChatUiEvent.OnRetryClick -> onRetryClick()

            ChatUiEvent.OnSubscribeClick -> emitAction(ChatUiAction.NavigateToRoute(PaywallNavRoute))

            ChatUiEvent.OnHistoryClick -> onHistoryClick()

            ChatUiEvent.OnHistoryDismiss -> updateHistory { it.copy(isOpen = false) }

            is ChatUiEvent.OnHistoryQueryChanged -> onHistoryQueryChanged(event.query)

            ChatUiEvent.OnNewConversationClick -> onNewConversationClick()

            is ChatUiEvent.OnConversationClick -> onConversationClick(event.conversationId)

            is ChatUiEvent.OnConversationActionsToggle -> updateHistory { history ->
                history.copy(
                    expandedActionsId = event.conversationId.takeIf { it != history.expandedActionsId },
                    renamingId = null,
                    deletingId = null,
                )
            }

            is ChatUiEvent.OnRenameConversationClick -> onRenameConversationClick(event.conversationId)

            is ChatUiEvent.OnRenameDraftChanged -> updateHistory { it.copy(renameDraft = event.title) }

            ChatUiEvent.OnRenameConfirm -> onRenameConfirm()

            ChatUiEvent.OnRenameCancel -> updateHistory {
                it.copy(
                    renamingId = null,
                    renameDraft = "",
                )
            }

            is ChatUiEvent.OnDeleteConversationClick -> updateHistory {
                it.copy(deletingId = event.conversationId)
            }

            ChatUiEvent.OnDeleteConfirm -> onDeleteConfirm()

            ChatUiEvent.OnDeleteCancel -> updateHistory { it.copy(deletingId = null) }
        }
    }

    private fun loadContext() {
        viewModelScope.launch {
            val loaded = useCases.getContext(dayRoute)
            context = loaded
            _uiState.update { it.copy(contextLabel = loaded?.label) }
            val studyQuestions = loaded
                ?.takeIf { usesStudyQuestions }
                ?.let { useCases.getSuggestions(it.passages) }
                .orEmpty()
            // The questions from the study come first, being the ones the reader just saw, and the
            // openers follow instead of being replaced: arriving from the study is no reason to
            // lose the only prompts on offer everywhere else.
            val suggestions = (studyQuestions + getDefaultSuggestions(hasReadingContext = loaded != null)).distinct()
            _uiState.update { state -> state.copy(suggestions = suggestions - usedSuggestions) }
            claimDayThread()
        }
    }

    private fun loadDefaultSuggestions() {
        viewModelScope.launch {
            val suggestions = getDefaultSuggestions(hasReadingContext = false)
            _uiState.update { state -> state.copy(suggestions = suggestions) }
        }
    }

    // Everything server-side needs an account, so the chat only reaches for it once there is one —
    // which also means signing in from the send gate loads the history and quota straight away.
    private fun observeAuthentication() {
        viewModelScope.launch {
            observeAuthenticatedUserId().collect { userId ->
                isLoggedIn = userId != null
                if (userId == null) return@collect
                useCases.refreshConversations()
                useCases.refreshQuota()
            }
        }
    }

    private fun observeConversations() {
        viewModelScope.launch {
            useCases.observeConversations().collect { loaded ->
                conversations = loaded
                claimDayThread()
                refreshHistoryGroups()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMessages() {
        viewModelScope.launch {
            activeConversationId
                .flatMapLatest { conversationId ->
                    if (conversationId == null) flowOf(emptyList()) else useCases.observeMessages(conversationId)
                }.collect { messages ->
                    _uiState.update { state ->
                        state.copy(
                            messages = messageUiMapper.map(messages),
                            isThinking = messageUiMapper.isThinking(messages) || state.pendingQuestion != null,
                        )
                    }
                    emitAction(ChatUiAction.ScrollToBottom)
                }
        }
    }

    private fun observeQuota() {
        viewModelScope.launch {
            useCases.observeQuota().collect(::onQuotaChanged)
        }
    }

    private fun onQuotaChanged(quota: ChatQuotaModel?) {
        _uiState.update { state ->
            state.copy(
                quota = quota
                    ?.takeIf { !it.isPro }
                    ?.let { ChatQuotaUiModel(it.remainingFree) },
                inputMode = when {
                    quota?.isExhausted == true -> ChatInputMode.LOCKED
                    state.inputMode == ChatInputMode.COOLDOWN -> ChatInputMode.COOLDOWN
                    else -> ChatInputMode.ENABLED
                },
            )
        }
    }

    private fun observeSend() {
        viewModelScope.launch {
            coordinator.send.collectLatest(::onSendChanged)
        }
    }

    private fun onSendChanged(send: ChatSendModel?) {
        // A conversation created by this send only gets its id once the server accepts it.
        send?.conversationId?.let { conversationId ->
            if (activeConversationId.value == null) activeConversationId.value = conversationId
        }
        // Until the server echoes the question back, the screen shows it locally so the send feels
        // instant; the same message then arrives with its real id and replaces this placeholder.
        val pending = send?.takeIf { !it.isAccepted && it.failure == null }?.request?.message
        _uiState.update { state ->
            state.copy(
                pendingQuestion = pending,
                isThinking = state.isThinking || pending != null,
                isAnswering = send != null && send.failure == null,
                failure = send?.failure,
            )
        }
        when (val failure = send?.failure) {
            is ChatSendFailureModel.RateLimited -> startCooldown(failure.retryAfterSeconds)
            ChatSendFailureModel.LimitReached -> _uiState.update { it.copy(inputMode = ChatInputMode.LOCKED) }
            ChatSendFailureModel.Generic, null -> Unit
        }
    }

    private fun syncRemoteChanges() {
        viewModelScope.launch { useCases.syncRemoteChanges() }
    }

    private fun onSuggestionClick(suggestion: String) {
        trackEvent(
            name = AnalyticsEventNames.AI_CHAT_SUGGESTION_CLICKED,
            params = emptyMap(),
        )
        // The chip is only spent when the question actually goes out: a tap that hits the login or
        // the cooldown gate leaves the suggestion on screen to be tapped again afterwards.
        if (!send(suggestion)) return
        usedSuggestions = usedSuggestions + suggestion
        _uiState.update { state -> state.copy(suggestions = state.suggestions - suggestion) }
    }

    private fun send(message: String): Boolean {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return false
        // Asking is what needs an account (the conversation is saved to it), so the gate sits here
        // rather than on the way in: a signed-out reader still gets the screen and the suggestions.
        if (!isLoggedIn) {
            emitAction(ChatUiAction.NavigateToRoute(LoginWarningNavRoute(LoginWarningReason.AiChat.key)))
            return false
        }
        if (_uiState.value.inputMode != ChatInputMode.ENABLED) return false
        // One answer at a time: the coordinator owns a single stream, so a second question sent now
        // would be dropped. Keep the text in the field instead of swallowing it.
        if (_uiState.value.isAnswering) return false
        val conversationId = activeConversationId.value
        trackEvent(
            name = AnalyticsEventNames.AI_CHAT_MESSAGE_SENT,
            params = mapOf(
                AnalyticsParams.HAS_CONTEXT to (context != null),
                AnalyticsParams.IS_NEW_CONVERSATION to (conversationId == null),
            ),
        )
        _uiState.update {
            it.copy(
                input = "",
                failure = null,
            )
        }
        coordinator.start(
            ChatSendRequestModel(
                conversationId = conversationId,
                message = trimmed,
                context = context,
            ),
        )
        return true
    }

    private fun onRetryClick() {
        _uiState.update { it.copy(failure = null) }
        coordinator.retry()
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    inputMode = ChatInputMode.COOLDOWN,
                    cooldownSeconds = seconds,
                )
            }
            while (_uiState.value.cooldownSeconds > 0) {
                delay(cooldownTick)
                _uiState.update { it.copy(cooldownSeconds = it.cooldownSeconds - 1) }
            }
            coordinator.clearFailure()
            _uiState.update {
                it.copy(
                    inputMode = ChatInputMode.ENABLED,
                    failure = null,
                )
            }
        }
    }

    private fun onHistoryClick() {
        trackEvent(
            name = AnalyticsEventNames.AI_CHAT_HISTORY_OPENED,
            params = mapOf(AnalyticsParams.COUNT to conversations.size),
        )
        updateHistory { it.copy(isOpen = true) }
        viewModelScope.launch { useCases.refreshConversations() }
    }

    private fun onHistoryQueryChanged(query: String) {
        updateHistory { it.copy(query = query) }
        refreshHistoryGroups()
    }

    /**
     * Starts a conversation with no reading behind it. Coming from a day would otherwise seed this
     * one with the same reading as the day's own thread, and the two would be indistinguishable —
     * asking for a new conversation is asking to leave that reading behind.
     */
    private fun onNewConversationClick() {
        activeConversationId.value = null
        usedSuggestions = emptySet()
        isDayThreadClaimed = true
        context = null
        _uiState.update { state ->
            state.copy(
                contextLabel = null,
                input = "",
                failure = null,
                suggestions = emptyList(),
                isSuggestionBarExpanded = false,
                history = state.history.copy(isOpen = false),
            )
        }
        loadDefaultSuggestions()
        refreshHistoryGroups()
    }

    /**
     * Reopens the conversation this day already has, so returning from the day's button or from the
     * study's questions card carries on where the reader left off rather than piling up a thread
     * per visit. The suggestions are not touched: they follow the door taken this time, which is
     * how the study's questions still show up in a thread that was started from the day screen.
     */
    private fun claimDayThread() {
        if (isDayThreadClaimed || activeConversationId.value != null) return
        val planDay = context?.planDay ?: return
        val existing = conversations.firstOrNull { it.planDay == planDay } ?: return
        isDayThreadClaimed = true
        openConversation(existing.id)
    }

    private fun onConversationClick(conversationId: String) {
        isDayThreadClaimed = true
        openConversation(conversationId)
    }

    private fun openConversation(conversationId: String) {
        activeConversationId.value = conversationId
        _uiState.update { state ->
            state.copy(
                contextLabel = conversations.firstOrNull { it.id == conversationId }?.contextLabel,
                failure = null,
                history = state.history.copy(isOpen = false),
            )
        }
        refreshHistoryGroups()
        viewModelScope.launch { useCases.loadMessages(conversationId) }
    }

    private fun onRenameConversationClick(conversationId: String) {
        updateHistory { history ->
            history.copy(
                renamingId = conversationId,
                renameDraft = conversations.firstOrNull { it.id == conversationId }?.title.orEmpty(),
                deletingId = null,
            )
        }
    }

    private fun onRenameConfirm() {
        val history = _uiState.value.history
        val conversationId = history.renamingId ?: return
        val title = history.renameDraft.trim()
        updateHistory {
            it.copy(
                renamingId = null,
                renameDraft = "",
                expandedActionsId = null,
            )
        }
        if (title.isEmpty()) return
        viewModelScope.launch {
            useCases.renameConversation(
                conversationId = conversationId,
                title = title,
            )
        }
    }

    private fun onDeleteConfirm() {
        val conversationId = _uiState.value.history.deletingId ?: return
        updateHistory {
            it.copy(
                deletingId = null,
                expandedActionsId = null,
            )
        }
        if (activeConversationId.value == conversationId) onNewConversationClick()
        viewModelScope.launch { useCases.deleteConversation(conversationId) }
    }

    private fun refreshHistoryGroups() {
        updateHistory { history ->
            history.copy(
                groups = conversationGroupMapper.map(
                    conversations = conversations,
                    activeConversationId = activeConversationId.value,
                    query = history.query,
                ),
                hasConversations = conversations.isNotEmpty(),
            )
        }
    }

    private fun updateHistory(transform: (ChatHistoryUiState) -> ChatHistoryUiState) {
        _uiState.update { state -> state.copy(history = transform(state.history)) }
    }

    private fun emitAction(action: ChatUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }
}
