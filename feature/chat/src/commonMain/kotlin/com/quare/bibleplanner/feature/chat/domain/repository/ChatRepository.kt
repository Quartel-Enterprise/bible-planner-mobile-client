package com.quare.bibleplanner.feature.chat.domain.repository

import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendEventModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeConversations(): Flow<List<ChatConversationModel>>

    fun observeMessages(conversationId: String): Flow<List<ChatMessageModel>>

    fun observeQuota(): Flow<ChatQuotaModel?>

    suspend fun syncRemoteChanges()

    suspend fun refreshConversations()

    suspend fun refreshMessages(conversationId: String)

    suspend fun refreshQuota()

    fun sendMessage(request: ChatSendRequestModel): Flow<ChatSendEventModel>

    suspend fun renameConversation(
        conversationId: String,
        title: String,
    )

    suspend fun deleteConversation(conversationId: String)

    fun observeDraft(threadKey: String): Flow<String>

    suspend fun saveDraft(
        threadKey: String,
        content: String,
    )
}
