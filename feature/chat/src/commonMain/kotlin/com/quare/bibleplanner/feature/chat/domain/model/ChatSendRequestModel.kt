package com.quare.bibleplanner.feature.chat.domain.model

data class ChatSendRequestModel(
    val conversationId: String?,
    val message: String,
    val context: ChatContextModel?,
)
