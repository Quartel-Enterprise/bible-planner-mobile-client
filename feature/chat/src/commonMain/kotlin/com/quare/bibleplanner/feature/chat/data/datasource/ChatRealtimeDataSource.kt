package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatDeletedRowDto
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
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

    fun observeConversations(userId: String): Flow<ChatRemoteChange> = observeTable(
        table = CONVERSATIONS_TABLE,
        userId = userId,
        upserted = { record: ChatConversationDto -> ChatRemoteChange.ConversationUpserted(record) },
        deleted = ChatRemoteChange::ConversationDeleted,
    )

    fun observeMessages(userId: String): Flow<ChatRemoteChange> = observeTable(
        table = MESSAGES_TABLE,
        userId = userId,
        upserted = { record: ChatMessageDto -> ChatRemoteChange.MessageUpserted(record) },
        deleted = ChatRemoteChange::MessageDeleted,
    )

    /**
     * Writes and deletions have to be subscribed separately, because they cannot share a filter.
     *
     * Inserts and updates are narrowed to this user server-side. Deletions cannot be: Postgres has
     * no row left to check a policy against, so Realtime strips the old record down to the primary
     * key — a `user_id` filter then matches nothing and the event is dropped before it is ever
     * sent. Listening unfiltered is what makes a deletion elsewhere arrive at all; the ids of other
     * readers' rows come with it, and deleting an id this device never had is a no-op.
     */
    private inline fun <reified T : Any> observeTable(
        table: String,
        userId: String,
        crossinline upserted: (T) -> ChatRemoteChange,
        crossinline deleted: (String) -> ChatRemoteChange,
    ): Flow<ChatRemoteChange> = flow {
        val channel = realtime.channel("${table}_$userId")
        val writes = channel.postgresChangeFlow<PostgresAction>(schema = SCHEMA) {
            this.table = table
            filter(USER_ID_COLUMN, FilterOperator.EQ, userId)
        }
        val deletions = channel.postgresChangeFlow<PostgresAction.Delete>(schema = SCHEMA) {
            this.table = table
        }
        channel.subscribe()
        try {
            merge(
                writes.mapNotNull { action ->
                    when (action) {
                        is PostgresAction.Insert -> upserted(action.decodeRecord())
                        is PostgresAction.Update -> upserted(action.decodeRecord())
                        else -> null
                    }
                },
                deletions.map { action -> deleted(action.decodeOldRecord<ChatDeletedRowDto>().id) },
            ).collect(::emit)
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
