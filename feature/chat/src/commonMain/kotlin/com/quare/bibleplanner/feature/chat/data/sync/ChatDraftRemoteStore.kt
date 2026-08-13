package com.quare.bibleplanner.feature.chat.data.sync

import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
import com.quare.bibleplanner.feature.chat.data.dto.ChatDraftDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

internal class ChatDraftRemoteStore(
    private val supabaseClient: SupabaseClient,
    private val realtime: Realtime,
) : SyncRemoteStore<ChatDraftDto> {
    override suspend fun upsert(dtos: List<ChatDraftDto>) {
        supabaseClient.from(TABLE).upsert(dtos) {
            onConflict = "user_id,thread_key"
        }
    }

    override suspend fun fetch(userId: String): List<ChatDraftDto> = supabaseClient
        .from(TABLE)
        .select {
            filter { eq("user_id", userId) }
        }.decodeList()

    override fun observeRemote(userId: String): Flow<ChatDraftDto> = flow {
        val channel = realtime.channel("${TABLE}_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = TABLE
            filter("user_id", FilterOperator.EQ, userId)
        }
        channel.subscribe()
        try {
            changes.collect { action ->
                val dto = when (action) {
                    is PostgresAction.Insert -> action.decodeRecord<ChatDraftDto>()
                    is PostgresAction.Update -> action.decodeRecord<ChatDraftDto>()
                    else -> null
                }
                if (dto != null) emit(dto)
            }
        } finally {
            withContext(NonCancellable) {
                realtime.removeChannel(channel)
            }
        }
    }

    private companion object {
        const val TABLE = "ai_chat_drafts"
    }
}
