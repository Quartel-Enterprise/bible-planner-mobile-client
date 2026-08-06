package com.quare.bibleplanner.feature.chat.presentation.model

import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel

data class ChatUiState(
    val contextLabel: String?,
    val messages: List<ChatMessageUiModel>,
    /** The question already sent but not yet echoed back by the server, shown right away. */
    val pendingQuestion: String?,
    val suggestions: List<String>,
    val isSuggestionBarExpanded: Boolean,
    val input: String,
    val inputMode: ChatInputMode,
    val cooldownSeconds: Int,
    val quota: ChatQuotaUiModel?,
    val isThinking: Boolean,
    /** An answer is on its way; a second question would be dropped, so sending is refused. */
    val isAnswering: Boolean,
    val failure: ChatSendFailureModel?,
    val history: ChatHistoryUiState,
) {
    /**
     * The local echo, dropped as soon as the same question comes back with its server id — the two
     * updates race, so this compares content instead of relying on their order.
     */
    val visiblePendingQuestion: String?
        get() = pendingQuestion?.takeIf { question ->
            messages.none { message -> message.isFromUser && message.text == question }
        }

    /** Before the first exchange the chips are laid out under the welcome message. */
    val showInitialSuggestions: Boolean
        get() = messages.isEmpty() && suggestions.isNotEmpty()

    val showSuggestionBar: Boolean
        get() = messages.isNotEmpty() && suggestions.isNotEmpty() && inputMode == ChatInputMode.ENABLED
}
