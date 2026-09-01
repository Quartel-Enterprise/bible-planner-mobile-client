package com.quare.bibleplanner.feature.chat.domain.model

sealed interface ChatSendEventModel {
    data class Accepted(
        val conversationId: String,
        val isNewConversation: Boolean,
    ) : ChatSendEventModel

    data object Completed : ChatSendEventModel
}
