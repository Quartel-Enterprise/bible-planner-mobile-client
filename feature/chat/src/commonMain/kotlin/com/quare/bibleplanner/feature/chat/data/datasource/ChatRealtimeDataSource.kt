package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto
import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeOldRecord
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Live conversation and message changes for the signed-in user, so a rename, a deletion or a
 * question asked on another device shows up here without a refresh. Two user-scoped channels
 * (rather than one per conversation) keep the history list fresh with a fixed number of
 * subscriptions.
 */
internal class ChatRealtimeDataSource(
    private val realtime: Realtime,
) {
    /**
     * Every transition into CONNECTED — cold start and each reconnection. It is the cue to pull a
     * fresh snapshot, since anything that changed while the socket was down was never delivered.
     */
    fun observeConnected(): Flow<Unit> = realtime.status
        .filter { status -> status == Realtime.Status.CONNECTED }
        .map { }

    fun observeConversations(userId: String): Flow<ChatRemoteChange> = flow {
        val channel = realtime.channel("${CONVERSATIONS_TABLE}_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = SCHEMA) {
            table = CONVERSATIONS_TABLE
            filter(USER_ID_COLUMN, FilterOperator.EQ, userId)
        }
        channel.subscribe()
        try {
            changes.collect { action ->
                val change = when (action) {
                    is PostgresAction.Insert -> ChatRemoteChange.ConversationUpserted(action.decodeRecord())

                    is PostgresAction.Update -> ChatRemoteChange.ConversationUpserted(action.decodeRecord())

                    // Delivered with the full old row thanks to `replica identity full` on the table.
                    is PostgresAction.Delete ->
                        ChatRemoteChange.ConversationDeleted(action.decodeOldRecord<ChatConversationDto>().id)

                    else -> null
                }
                if (change != null) emit(change)
            }
        } finally {
            withContext(NonCancellable) {
                realtime.removeChannel(channel)
            }
        }
    }

    fun observeMessages(userId: String): Flow<ChatRemoteChange> = flow {
        val channel = realtime.channel("${MESSAGES_TABLE}_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = SCHEMA) {
            table = MESSAGES_TABLE
            filter(USER_ID_COLUMN, FilterOperator.EQ, userId)
        }
        channel.subscribe()
        try {
            changes.collect { action ->
                val change = when (action) {
                    is PostgresAction.Insert -> ChatRemoteChange.MessageUpserted(action.decodeRecord())

                    is PostgresAction.Delete ->
                        ChatRemoteChange.MessageDeleted(action.decodeOldRecord<ChatMessageDto>().id)

                    else -> null
                }
                if (change != null) emit(change)
            }
        } finally {
            withContext(NonCancellable) {
                realtime.removeChannel(channel)
            }
        }
    }

    private companion object {
        const val SCHEMA = "public"
        const val CONVERSATIONS_TABLE = "ai_chat_conversations"
        const val MESSAGES_TABLE = "ai_chat_messages"
        const val USER_ID_COLUMN = "user_id"
    }
}
