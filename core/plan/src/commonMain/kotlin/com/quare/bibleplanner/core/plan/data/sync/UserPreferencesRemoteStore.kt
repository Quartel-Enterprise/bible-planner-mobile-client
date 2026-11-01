package com.quare.bibleplanner.core.plan.data.sync

import com.quare.bibleplanner.core.plan.data.dto.UserPreferenceDto
import com.quare.bibleplanner.core.provider.supabase.USER_ID_COLUMN
import com.quare.bibleplanner.core.provider.supabase.observeUserUpserts
import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.flow.Flow

internal class UserPreferencesRemoteStore(
    private val supabaseClient: SupabaseClient,
    private val realtime: Realtime,
) : SyncRemoteStore<UserPreferenceDto> {
    override suspend fun upsert(dtos: List<UserPreferenceDto>) {
        supabaseClient.from(TABLE).upsert(dtos) {
            onConflict = "user_id,key"
        }
    }

    override suspend fun fetch(userId: String): List<UserPreferenceDto> = supabaseClient
        .from(TABLE)
        .select {
            filter { eq(USER_ID_COLUMN, userId) }
        }.decodeList()

    override fun observeRemote(userId: String): Flow<UserPreferenceDto> = realtime.observeUserUpserts(
        table = TABLE,
        userId = userId,
    )

    private companion object {
        const val TABLE = "user_preferences"
    }
}
