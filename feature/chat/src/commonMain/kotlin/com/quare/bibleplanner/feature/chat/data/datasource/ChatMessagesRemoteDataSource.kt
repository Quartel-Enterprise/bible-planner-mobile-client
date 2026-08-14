package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto

internal interface ChatMessagesRemoteDataSource {
    suspend fun fetchByConversation(conversationId: String): List<ChatMessageDto>
}
