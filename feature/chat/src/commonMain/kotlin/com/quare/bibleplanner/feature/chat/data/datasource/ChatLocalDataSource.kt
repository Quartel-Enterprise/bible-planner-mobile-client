package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import kotlinx.coroutines.flow.Flow

internal interface ChatLocalDataSource {
    fun observeConversations(): Flow<List<ChatConversationModel>>

    fun observeMessages(conversationId: String): Flow<List<ChatMessageModel>>

    suspend fun replaceConversations(conversations: List<ChatConversationModel>)

    suspend fun saveConversation(conversation: ChatConversationModel)

    suspend fun replaceMessages(
        conversationId: String,
        messages: List<ChatMessageModel>,
    )

    suspend fun saveMessage(
        conversationId: String,
        message: ChatMessageModel,
    )

    suspend fun deleteMessage(messageId: String)

    suspend fun deleteConversation(conversationId: String)

    suspend fun deleteAll()
}
