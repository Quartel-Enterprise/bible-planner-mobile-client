package com.quare.bibleplanner.core.books.data.sync

import com.quare.bibleplanner.core.books.data.dto.VerseReadDto
import com.quare.bibleplanner.core.provider.supabase.USER_ID_COLUMN
import com.quare.bibleplanner.core.provider.supabase.observeUserUpserts
import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.flow.Flow

internal class VerseReadRemoteStore(
    private val supabaseClient: SupabaseClient,
    private val realtime: Realtime,
) : SyncRemoteStore<VerseReadDto> {
    override suspend fun upsert(dtos: List<VerseReadDto>) {
        supabaseClient.from(TABLE).upsert(dtos) {
            onConflict = "user_id,book_id,chapter_number,verse_number"
        }
    }

    override suspend fun fetch(userId: String): List<VerseReadDto> = supabaseClient
        .from(TABLE)
        .select {
            filter { eq(USER_ID_COLUMN, userId) }
        }.decodeList()

    override fun observeRemote(userId: String): Flow<VerseReadDto> = realtime.observeUserUpserts(
        table = TABLE,
        userId = userId,
    )

    private companion object {
        const val TABLE = "verse_reads"
    }
}
