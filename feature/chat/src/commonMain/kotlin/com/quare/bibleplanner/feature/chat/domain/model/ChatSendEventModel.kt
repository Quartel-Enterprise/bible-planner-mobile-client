package com.quare.bibleplanner.feature.chat.domain.model

sealed interface ChatSendEventModel {
    /** The question was persisted and generation started; a new conversation now has an id. */
    data class Accepted(
        val conversationId: String,
        val isNewConversation: Boolean,
    ) : ChatSendEventModel

    data object Completed : ChatSendEventModel
}
