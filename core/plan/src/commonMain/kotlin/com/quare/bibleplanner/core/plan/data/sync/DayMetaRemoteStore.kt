package com.quare.bibleplanner.core.plan.data.sync

import com.quare.bibleplanner.core.plan.data.dto.DayMetaDto
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

internal class DayMetaRemoteStore(
    private val supabaseClient: SupabaseClient,
    private val realtime: Realtime,
) : SyncRemoteStore<DayMetaDto> {
    override suspend fun upsert(dtos: List<DayMetaDto>) {
        supabaseClient.from(TABLE).upsert(dtos) {
            onConflict = "user_id,week_number,day_number,plan_type"
        }
    }

    override suspend fun fetch(userId: String): List<DayMetaDto> = supabaseClient
        .from(TABLE)
        .select {
            filter { eq("user_id", userId) }
        }.decodeList()

    override fun observeRemote(userId: String): Flow<DayMetaDto> = flow {
        val channel = realtime.channel("day_meta_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = TABLE
            filter("user_id", FilterOperator.EQ, userId)
        }
        channel.subscribe()
        try {
            changes.collect { action ->
                val dto = when (action) {
                    is PostgresAction.Insert -> action.decodeRecord<DayMetaDto>()
                    is PostgresAction.Update -> action.decodeRecord<DayMetaDto>()
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
        const val TABLE = "day_meta"
    }
}
