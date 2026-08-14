package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto

internal interface ChatConversationsRemoteDataSource {
    suspend fun fetchAll(): List<ChatConversationDto>

    suspend fun rename(
        conversationId: String,
        title: String,
    )

    suspend fun delete(conversationId: String)
}
