package com.quare.bibleplanner.feature.chat.presentation.model

import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel

data class ChatUiState(
    val contextLabel: String?,
    val messages: List<ChatMessageUiModel>,
    val pendingQuestion: String?,
    val suggestions: List<String>,
    val isSuggestionBarExpanded: Boolean,
    val input: String,
    val inputMode: ChatInputMode,
    val cooldownSeconds: Int,
    val quota: ChatQuotaUiModel?,
    val isThinking: Boolean,
    val isAnswering: Boolean,
    val failure: ChatSendFailureModel?,
    val history: ChatHistoryUiState,
) {
    val visiblePendingQuestion: String?
        get() = pendingQuestion?.takeIf { question ->
            messages.none { message -> message.isFromUser && message.text == question }
        }

    val showInitialSuggestions: Boolean
        get() = messages.isEmpty() && !isAnswering && suggestions.isNotEmpty()

    val showSuggestionBar: Boolean
        get() = messages.isNotEmpty() && suggestions.isNotEmpty() && inputMode == ChatInputMode.ENABLED
}
