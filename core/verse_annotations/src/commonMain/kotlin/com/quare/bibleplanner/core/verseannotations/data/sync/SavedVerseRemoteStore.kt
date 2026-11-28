package com.quare.bibleplanner.core.verseannotations.data.sync

import com.quare.bibleplanner.core.provider.supabase.USER_ID_COLUMN
import com.quare.bibleplanner.core.provider.supabase.observeUserUpserts
import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
import com.quare.bibleplanner.core.verseannotations.data.dto.SavedVerseDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.flow.Flow

internal class SavedVerseRemoteStore(
    private val supabaseClient: SupabaseClient,
    private val realtime: Realtime,
) : SyncRemoteStore<SavedVerseDto> {
    override suspend fun upsert(dtos: List<SavedVerseDto>) {
        supabaseClient.from(TABLE).upsert(dtos) {
            onConflict = "user_id,bible_version_id,book_id,chapter_number,verse_number"
        }
    }

    override suspend fun fetch(userId: String): List<SavedVerseDto> = supabaseClient
        .from(TABLE)
        .select {
            filter { eq(USER_ID_COLUMN, userId) }
        }.decodeList()

    override fun observeRemote(userId: String): Flow<SavedVerseDto> = realtime.observeUserUpserts(
        table = TABLE,
        userId = userId,
    )

    private companion object {
        const val TABLE = "saved_verses"
    }
}
