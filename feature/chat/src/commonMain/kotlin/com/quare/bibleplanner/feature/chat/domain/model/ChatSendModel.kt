package com.quare.bibleplanner.feature.chat.domain.model

/**
 * The send currently owned by the coordinator: it outlives the screen, so leaving the chat and
 * coming back re-attaches to the same in-flight answer instead of losing it.
 */
data class ChatSendModel(
    val request: ChatSendRequestModel,
    val conversationId: String?,
    /** True once the server has persisted the question and generation is under way. */
    val isAccepted: Boolean,
    val isStreaming: Boolean,
    val failure: ChatSendFailureModel?,
)
