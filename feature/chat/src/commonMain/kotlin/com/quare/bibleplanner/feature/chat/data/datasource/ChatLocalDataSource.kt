package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.core.provider.room.dao.ChatDao
import com.quare.bibleplanner.feature.chat.data.mapper.ChatEntityMapper
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The offline copy of the chat. It is a mirror, never a source of writes for the server: everything
 * here arrived from a fetch, from Realtime or from an answer the server already confirmed.
 */
internal class ChatLocalDataSource(
    private val chatDao: ChatDao,
    private val mapper: ChatEntityMapper,
) {
    fun observeConversations(): Flow<List<ChatConversationModel>> = chatDao
        .observeConversations()
        .map { entities -> entities.map(mapper::mapConversation) }

    fun observeMessages(conversationId: String): Flow<List<ChatMessageModel>> = chatDao
        .observeMessages(conversationId)
        .map { entities -> entities.map(mapper::mapMessage) }

    suspend fun replaceConversations(conversations: List<ChatConversationModel>) {
        chatDao.replaceConversations(conversations.map(mapper::mapConversation))
    }

    suspend fun saveConversation(conversation: ChatConversationModel) {
        chatDao.upsertConversations(listOf(mapper.mapConversation(conversation)))
    }

    suspend fun replaceMessages(
        conversationId: String,
        messages: List<ChatMessageModel>,
    ) {
        chatDao.replaceMessages(
            conversationId = conversationId,
            messages = messages.map { message ->
                mapper.mapMessage(
                    conversationId = conversationId,
                    model = message,
                )
            },
        )
    }

    suspend fun saveMessage(
        conversationId: String,
        message: ChatMessageModel,
    ) {
        chatDao.upsertMessages(
            listOf(
                mapper.mapMessage(
                    conversationId = conversationId,
                    model = message,
                ),
            ),
        )
    }

    suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessage(messageId)
    }

    suspend fun deleteConversation(conversationId: String) {
        chatDao.deleteConversation(conversationId)
    }

    suspend fun deleteAll() {
        chatDao.deleteAll()
    }
}
