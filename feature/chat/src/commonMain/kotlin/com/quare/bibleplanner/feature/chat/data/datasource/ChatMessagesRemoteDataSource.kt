package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto

internal fun interface ChatMessagesRemoteDataSource {
    suspend fun fetchByConversation(conversationId: String): List<ChatMessageDto>
}
