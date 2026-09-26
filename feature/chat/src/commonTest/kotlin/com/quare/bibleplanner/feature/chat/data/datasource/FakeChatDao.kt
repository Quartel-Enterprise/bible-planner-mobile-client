package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.core.provider.room.dao.ChatDao
import com.quare.bibleplanner.core.provider.room.entity.ChatConversationEntity
import com.quare.bibleplanner.core.provider.room.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeChatDao : ChatDao {
    val conversations: MutableStateFlow<List<ChatConversationEntity>> = MutableStateFlow(emptyList())
    val messages: MutableStateFlow<List<ChatMessageEntity>> = MutableStateFlow(emptyList())

    override fun observeConversations(): Flow<List<ChatConversationEntity>> = conversations

    override fun observeMessages(conversationId: String): Flow<List<ChatMessageEntity>> = messages
        .map { current -> current.filter { it.conversationId == conversationId } }

    override suspend fun upsertConversations(conversations: List<ChatConversationEntity>) {
        val ids = conversations.map(ChatConversationEntity::id).toSet()
        this.conversations.value = this.conversations.value.filterNot { it.id in ids } + conversations
    }

    override suspend fun upsertMessages(messages: List<ChatMessageEntity>) {
        val ids = messages.map(ChatMessageEntity::id).toSet()
        this.messages.value = this.messages.value.filterNot { it.id in ids } + messages
    }

    override suspend fun deleteConversation(conversationId: String) {
        conversations.value = conversations.value.filterNot { it.id == conversationId }
        deleteMessagesOf(conversationId)
    }

    override suspend fun deleteMessage(messageId: String) {
        messages.value = messages.value.filterNot { it.id == messageId }
    }

    override suspend fun deleteConversationsNotIn(conversationIds: List<String>) {
        conversations.value = conversations.value.filter { it.id in conversationIds }
    }

    override suspend fun deleteAllConversations() {
        conversations.value = emptyList()
        messages.value = emptyList()
    }

    override suspend fun deleteMessagesOf(conversationId: String) {
        messages.value = messages.value.filterNot { it.conversationId == conversationId }
    }

    override suspend fun deleteMessagesNotIn(
        conversationId: String,
        messageIds: List<String>,
    ) {
        messages.value = messages.value.filter { it.conversationId != conversationId || it.id in messageIds }
    }
}
