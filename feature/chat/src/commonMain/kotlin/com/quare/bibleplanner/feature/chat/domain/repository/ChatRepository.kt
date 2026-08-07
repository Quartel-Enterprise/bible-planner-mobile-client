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

    /** Runs until cancelled, applying the user's remote changes (other devices) to the session. */
    suspend fun syncRemoteChanges()

    suspend fun refreshConversations()

    suspend fun refreshMessages(conversationId: String)

    suspend fun refreshQuota()

    /** Streams one answer, growing the assistant message in the session as fragments arrive. */
    fun sendMessage(request: ChatSendRequestModel): Flow<ChatSendEventModel>

    suspend fun renameConversation(
        conversationId: String,
        title: String,
    )

    suspend fun deleteConversation(conversationId: String)
}
