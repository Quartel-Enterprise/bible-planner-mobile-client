package com.quare.bibleplanner.feature.chat.domain.model

data class ChatSendModel(
    val request: ChatSendRequestModel,
    val conversationId: String?,
    val isAccepted: Boolean,
    val isStreaming: Boolean,
    val failure: ChatSendFailureModel?,
)
