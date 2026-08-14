package com.quare.bibleplanner.feature.chat.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
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
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.feature.chat.domain.coordinator.ChatStreamCoordinator
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
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
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class ChatViewModel(
    route: ChatNavRoute,
    private val useCases: ChatUseCases,
    private val applicationScope: ApplicationScope,
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
    private val draftDebounceDelay: Duration = 2.seconds
    private val answerWaitWindow: Duration = 2.minutes
    private val answerWaitRecheck: Duration = 5.seconds

    private val dayRoute = route.toDayNavRoute()

    private val activeConversationId: MutableStateFlow<String?> = MutableStateFlow(null)
    private var conversations: List<ChatConversationModel> = emptyList()
    private var context: ChatContextModel? = null
    private var dayContext: ChatContextModel? = null
    private var usedSuggestions: Set<String> = emptySet()

    private var isDayThreadClaimed = false
    private var cooldownJob: Job? = null
    private var draftSaveJob: Job? = null
    private var pendingDraft: String? = null
    private var lastAppliedDraft: String? = null
    private var threadMessages: List<ChatMessageModel> = emptyList()
    private var newestMessage: Pair<String, Boolean>? = null
    private var awaitingAnswerJob: Job? = null
    private val currentThreadKey: MutableStateFlow<String> = MutableStateFlow(NEW_THREAD_KEY)
    private var isLoggedIn: Boolean = false

    init {
        loadContext()
        observeAuthentication()
        observeConversations()
        observeMessages()
        observeQuota()
        observeSend()
        syncRemoteChanges()
        observeDraft()
    }

    override fun onCleared() {
        super.onCleared()
        val unsaved = pendingDraft ?: return
        if (draftSaveJob?.isActive != true) return
        draftSaveJob?.cancel()
        val key = currentThreadKey.value
        applicationScope.launch { useCases.saveDraft(key, unsaved) }
    }

    override fun handleEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.OnInputChanged -> onInputChanged(event.text)

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

    private fun refreshThreadKey() {
        currentThreadKey.value = activeConversationId.value
            ?: context?.planDay?.let { "$DAY_THREAD_KEY_PREFIX:${it.readingPlanType}:${it.weekNumber}:${it.dayNumber}" }
            ?: NEW_THREAD_KEY
    }

    private fun onInputChanged(text: String) {
        _uiState.update { it.copy(input = text) }
        pendingDraft = text
        draftSaveJob?.cancel()
        val key = currentThreadKey.value
        draftSaveJob = viewModelScope.launch {
            delay(draftDebounceDelay)
            useCases.saveDraft(key, text)
            pendingDraft = null
        }
    }

    private fun observeDraft() {
        viewModelScope.launch {
            currentThreadKey
                .flatMapLatest(useCases.observeDraft::invoke)
                .collect(::onDraftChanged)
        }
    }

    private fun onDraftChanged(draft: String) {
        val input = _uiState.value.input
        if (input == draft) {
            lastAppliedDraft = draft
            return
        }
        if (input.isNotEmpty() && input != lastAppliedDraft) return
        lastAppliedDraft = draft
        _uiState.update { it.copy(input = draft) }
    }

    private fun clearDraft() {
        pendingDraft = null
        draftSaveJob?.cancel()
        val key = currentThreadKey.value
        viewModelScope.launch { useCases.saveDraft(key, "") }
    }

    private fun loadContext() {
        viewModelScope.launch {
            val loaded = useCases.getContext(dayRoute)
            dayContext = loaded
            context = loaded
            refreshThreadKey()
            _uiState.update { it.copy(contextLabel = loaded?.label) }
            val studyQuestions = loaded?.let { useCases.getSuggestions(it.passages) }.orEmpty()
            val suggestions = (studyQuestions + getDefaultSuggestions(hasReadingContext = loaded != null)).distinct()
            _uiState.update { state -> state.copy(suggestions = suggestions - usedSuggestions) }
            claimDayThread()
        }
    }

    private fun loadSuggestions() {
        val reading = context ?: return
        viewModelScope.launch {
            val studyQuestions = useCases.getSuggestions(reading.passages)
            val suggestions = (studyQuestions + getDefaultSuggestions(hasReadingContext = true)).distinct()
            _uiState.update { state -> state.copy(suggestions = suggestions - usedSuggestions) }
        }
    }

    private fun loadDefaultSuggestions() {
        viewModelScope.launch {
            val suggestions = getDefaultSuggestions(hasReadingContext = false)
            _uiState.update { state -> state.copy(suggestions = suggestions) }
        }
    }

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
                }.collect(::applyMessages)
        }
    }

    private fun applyMessages(messages: List<ChatMessageModel>) {
        threadMessages = messages
        val isAwaitingAnswer = messages.isAwaitingAnswer()
        _uiState.update { state ->
            state.copy(
                messages = messageUiMapper.map(messages),
                isThinking = state.isThinkingWith(
                    messages = messages,
                    isAwaitingAnswer = isAwaitingAnswer,
                ),
            )
        }
        watchAwaitingAnswer(isAwaitingAnswer)
        scrollOnNewMessage(messages)
    }

    /**
     * A message arriving — the reader's own or the answer to it — brings the thread to its end. The
     * answer being written does not, or every token would drag the list out from under whoever is
     * reading further up; it lands once when it appears and again when it is finished.
     */
    private fun scrollOnNewMessage(messages: List<ChatMessageModel>) {
        val newest = messages.lastOrNull()?.let { message -> message.id to message.isStreaming }
        if (newest == null || newest == newestMessage) return
        newestMessage = newest
        emitAction(ChatUiAction.ScrollToBottom)
    }

    private fun ChatUiState.isThinkingWith(
        messages: List<ChatMessageModel>,
        isAwaitingAnswer: Boolean,
    ): Boolean = messageUiMapper.isThinking(messages) || pendingQuestion != null || isAwaitingAnswer

    private fun List<ChatMessageModel>.isAwaitingAnswer(): Boolean {
        val last = lastOrNull() ?: return false
        if (last.role != ChatRoleModel.USER) return false
        return Clock.System.now() - last.createdAt < answerWaitWindow
    }

    private fun watchAwaitingAnswer(isAwaitingAnswer: Boolean) {
        if (!isAwaitingAnswer) {
            awaitingAnswerJob?.cancel()
            awaitingAnswerJob = null
            return
        }
        if (awaitingAnswerJob?.isActive == true) return
        awaitingAnswerJob = viewModelScope.launch {
            while (threadMessages.isAwaitingAnswer()) {
                delay(answerWaitRecheck)
            }
            _uiState.update { state ->
                state.copy(
                    isThinking = state.isThinkingWith(
                        messages = threadMessages,
                        isAwaitingAnswer = false,
                    ),
                )
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
        send?.conversationId?.let { conversationId ->
            if (activeConversationId.value == null) activeConversationId.value = conversationId
        }
        val pending = send?.takeIf { !it.isAccepted && it.failure == null }?.request?.message
        val hadPendingQuestion = _uiState.value.pendingQuestion != null
        _uiState.update { state ->
            state.copy(
                pendingQuestion = pending,
                isThinking = state.isThinking || pending != null,
                isAnswering = send != null && send.failure == null,
                failure = send?.failure?.takeUnless { send.isAccepted && it is ChatSendFailureModel.Generic },
            )
        }
        // The question shows up in the thread before the server echoes it back, and it belongs at the
        // bottom where the reader is looking.
        if (pending != null && !hadPendingQuestion) emitAction(ChatUiAction.ScrollToBottom)
        when (val failure = send?.failure) {
            is ChatSendFailureModel.RateLimited -> startCooldown(failure.retryAfterSeconds)
            ChatSendFailureModel.LimitReached -> _uiState.update { it.copy(inputMode = ChatInputMode.LOCKED) }
            ChatSendFailureModel.ConversationGone, ChatSendFailureModel.Generic, null -> Unit
        }
    }

    private fun syncRemoteChanges() {
        useCases.syncRemoteChanges()
    }

    private fun onSuggestionClick(suggestion: String) {
        trackEvent(
            name = AnalyticsEventNames.AI_CHAT_SUGGESTION_CLICKED,
            params = emptyMap(),
        )
        if (!send(suggestion)) return
        usedSuggestions = usedSuggestions + suggestion
        _uiState.update { state -> state.copy(suggestions = state.suggestions - suggestion) }
    }

    private fun send(message: String): Boolean {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return false
        if (!isLoggedIn) {
            emitAction(ChatUiAction.NavigateToRoute(LoginWarningNavRoute(LoginWarningReason.AiChat.key)))
            return false
        }
        if (_uiState.value.inputMode != ChatInputMode.ENABLED) return false
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
                isSuggestionBarExpanded = false,
            )
        }
        clearDraft()
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
        if (coordinator.send.value?.failure != null) {
            coordinator.retry()
            return
        }
        threadMessages.lastOrNull { it.role == ChatRoleModel.USER }?.let { question -> send(question.content) }
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

    private fun onNewConversationClick() {
        activeConversationId.value = null
        usedSuggestions = emptySet()
        isDayThreadClaimed = true
        context = null
        refreshThreadKey()
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
        val conversation = conversations.firstOrNull { it.id == conversationId }
        activeConversationId.value = conversationId
        refreshThreadKey()
        _uiState.update { state ->
            state.copy(
                contextLabel = conversation?.contextLabel,
                failure = null,
                history = state.history.copy(isOpen = false),
            )
        }
        restoreContextOf(conversation)
        refreshHistoryGroups()
        viewModelScope.launch { useCases.loadMessages(conversationId) }
    }

    /**
     * Puts back the reading a conversation belongs to, which starting a fresh one had cleared away.
     * Only for this screen's own day: another day's thread would need its passages, which are not
     * loaded here, and offering this day's questions under it would be worse than offering none.
     */
    private fun restoreContextOf(conversation: ChatConversationModel?) {
        if (context != null) return
        if (conversation?.planDay == null || conversation.planDay != dayContext?.planDay) return
        context = dayContext
        loadSuggestions()
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
        viewModelScope.launch {
            useCases.deleteConversation(conversationId).onFailure { throwable ->
                Logger.e(throwable) { "Failed to delete the conversation" }
                useCases.refreshConversations()
            }
        }
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

    private companion object {
        const val DAY_THREAD_KEY_PREFIX = "day"
        const val NEW_THREAD_KEY = "new"
    }
}
