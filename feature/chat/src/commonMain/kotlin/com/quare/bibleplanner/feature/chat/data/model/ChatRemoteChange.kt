package com.quare.bibleplanner.feature.chat.data.model

import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto

internal sealed interface ChatRemoteChange {
    data class ConversationUpserted(
        val conversation: ChatConversationDto,
    ) : ChatRemoteChange

    data class ConversationDeleted(
        val conversationId: String,
    ) : ChatRemoteChange

    data class MessageUpserted(
        val message: ChatMessageDto,
    ) : ChatRemoteChange

    data class MessageDeleted(
        val messageId: String,
    ) : ChatRemoteChange
}
