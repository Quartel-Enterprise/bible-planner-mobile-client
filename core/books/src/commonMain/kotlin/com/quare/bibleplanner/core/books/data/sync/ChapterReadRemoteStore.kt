package com.quare.bibleplanner.core.books.data.sync

import com.quare.bibleplanner.core.books.data.dto.ChapterReadDto
import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
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

internal class ChapterReadRemoteStore(
    private val supabaseClient: SupabaseClient,
    private val realtime: Realtime,
) : SyncRemoteStore<ChapterReadDto> {
    override suspend fun upsert(dtos: List<ChapterReadDto>) {
        supabaseClient.from(TABLE).upsert(dtos) {
            onConflict = "user_id,book_id,chapter_number"
        }
    }

    override suspend fun fetch(userId: String): List<ChapterReadDto> = supabaseClient
        .from(TABLE)
        .select {
            filter { eq("user_id", userId) }
        }.decodeList()

    override fun observeRemote(userId: String): Flow<ChapterReadDto> = flow {
        val channel = realtime.channel("chapter_reads_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = TABLE
            filter("user_id", FilterOperator.EQ, userId)
        }
        channel.subscribe()
        try {
            changes.collect { action ->
                val dto = when (action) {
                    is PostgresAction.Insert -> action.decodeRecord<ChapterReadDto>()
                    is PostgresAction.Update -> action.decodeRecord<ChapterReadDto>()
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
        const val TABLE = "chapter_reads"
    }
}
