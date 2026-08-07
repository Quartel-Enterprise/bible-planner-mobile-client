package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.quare.bibleplanner.core.provider.room.entity.ChatConversationEntity
import com.quare.bibleplanner.core.provider.room.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_conversations ORDER BY updatedAtEpochMillis DESC")
    fun observeConversations(): Flow<List<ChatConversationEntity>>

    @Query(
        // On identical timestamps the question comes before its answer (isFromUser first), so a
        // tie can never read as the assistant replying before it was asked.
        "SELECT * FROM chat_messages WHERE conversationId = :conversationId " +
            "ORDER BY createdAtEpochMillis ASC, isFromUser DESC, id ASC",
    )
    fun observeMessages(conversationId: String): Flow<List<ChatMessageEntity>>

    @Upsert
    suspend fun upsertConversations(conversations: List<ChatConversationEntity>)

    @Upsert
    suspend fun upsertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chat_conversations WHERE id NOT IN (:conversationIds)")
    suspend fun deleteConversationsNotIn(conversationIds: List<String>)

    @Query("DELETE FROM chat_conversations")
    suspend fun deleteAllConversations()

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesOf(conversationId: String)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId AND id NOT IN (:messageIds)")
    suspend fun deleteMessagesNotIn(
        conversationId: String,
        messageIds: List<String>,
    )

    /**
     * Mirrors the server's conversation list, dropping the ones deleted elsewhere — the reason this
     * cache is reconciled rather than merely upserted. Their messages go with them (cascade).
     */
    @Transaction
    suspend fun replaceConversations(conversations: List<ChatConversationEntity>) {
        if (conversations.isEmpty()) {
            deleteAllConversations()
            return
        }
        deleteConversationsNotIn(conversations.map(ChatConversationEntity::id))
        upsertConversations(conversations)
    }

    /** Same reconciliation for one thread: a question dropped by a failed generation disappears. */
    @Transaction
    suspend fun replaceMessages(
        conversationId: String,
        messages: List<ChatMessageEntity>,
    ) {
        // An empty list has to take the unfiltered delete: Room renders `NOT IN ()` for it, which
        // SQLite rejects outright.
        if (messages.isEmpty()) {
            deleteMessagesOf(conversationId)
            return
        }
        deleteMessagesNotIn(
            conversationId = conversationId,
            messageIds = messages.map(ChatMessageEntity::id),
        )
        upsertMessages(messages)
    }

    @Transaction
    suspend fun deleteAll() {
        deleteAllConversations()
    }
}
