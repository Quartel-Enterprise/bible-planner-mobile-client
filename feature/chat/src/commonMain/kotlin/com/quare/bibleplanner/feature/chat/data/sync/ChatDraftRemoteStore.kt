package com.quare.bibleplanner.feature.chat.data.sync

import com.quare.bibleplanner.core.provider.supabase.USER_ID_COLUMN
import com.quare.bibleplanner.core.provider.supabase.observeUserUpserts
import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
import com.quare.bibleplanner.feature.chat.data.dto.ChatDraftDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.flow.Flow

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
            filter { eq(USER_ID_COLUMN, userId) }
        }.decodeList()

    override fun observeRemote(userId: String): Flow<ChatDraftDto> = realtime.observeUserUpserts(
        table = TABLE,
        userId = userId,
    )

    private companion object {
        const val TABLE = "ai_chat_drafts"
    }
}
