package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.core.provider.room.dao.ChatDao
import com.quare.bibleplanner.feature.chat.data.mapper.ChatEntityMapper
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ChatLocalDataSourceImpl(
    private val chatDao: ChatDao,
    private val mapper: ChatEntityMapper,
) : ChatLocalDataSource {
    override fun observeConversations(): Flow<List<ChatConversationModel>> = chatDao
        .observeConversations()
        .map { entities -> entities.map(mapper::mapConversation) }

    override fun observeMessages(conversationId: String): Flow<List<ChatMessageModel>> = chatDao
        .observeMessages(conversationId)
        .map { entities -> entities.map(mapper::mapMessage) }

    override suspend fun replaceConversations(conversations: List<ChatConversationModel>) {
        chatDao.replaceConversations(conversations.map(mapper::mapConversation))
    }

    override suspend fun saveConversation(conversation: ChatConversationModel) {
        chatDao.upsertConversations(listOf(mapper.mapConversation(conversation)))
    }

    override suspend fun replaceMessages(
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

    override suspend fun saveMessage(
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

    override suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessage(messageId)
    }

    override suspend fun deleteConversation(conversationId: String) {
        chatDao.deleteConversation(conversationId)
    }

    override suspend fun deleteAll() {
        chatDao.deleteAll()
    }
}
