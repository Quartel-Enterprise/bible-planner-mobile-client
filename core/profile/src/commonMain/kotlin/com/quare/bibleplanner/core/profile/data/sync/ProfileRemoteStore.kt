package com.quare.bibleplanner.core.profile.data.sync

import com.quare.bibleplanner.core.profile.data.dto.ProfileDto
import com.quare.bibleplanner.core.profile.data.dto.ProfileRowDto
import com.quare.bibleplanner.core.profile.data.mapper.ProfileMapper
import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal class ProfileRemoteStore(
    private val supabaseClient: SupabaseClient,
    private val profileMapper: ProfileMapper,
) : SyncRemoteStore<ProfileDto> {
    private val realtime: Realtime
        get() = supabaseClient.realtime

    override suspend fun upsert(dtos: List<ProfileDto>) {
        dtos
            .filter { it.isDisplayNameDirty || it.isAvatarDirty }
            .forEach { dto ->
                supabaseClient.from(TABLE).upsert(dto.toPayload()) {
                    onConflict = COLUMN_ID
                }
            }
    }

    override suspend fun fetch(userId: String): List<ProfileDto> = supabaseClient
        .from(TABLE)
        .select(columns = COLUMNS) {
            filter { eq(COLUMN_ID, userId) }
        }.decodeList<ProfileRowDto>()
        .map(profileMapper::toDto)

    override fun observeRemote(userId: String): Flow<ProfileDto> = flow {
        val channel = realtime.channel("${TABLE}_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = SCHEMA) {
            table = TABLE
            filter(COLUMN_ID, FilterOperator.EQ, userId)
        }
        channel.subscribe()
        try {
            changes.collect { action ->
                val row = when (action) {
                    is PostgresAction.Insert -> action.decodeRecord<ProfileRowDto>()
                    is PostgresAction.Update -> action.decodeRecord<ProfileRowDto>()
                    else -> null
                }
                if (row != null) emit(profileMapper.toDto(row))
            }
        } finally {
            withContext(NonCancellable) {
                realtime.removeChannel(channel)
            }
        }
    }

    private fun ProfileDto.toPayload(): JsonObject = JsonObject(
        buildMap {
            put(COLUMN_ID, JsonPrimitive(id))
            put(COLUMN_UPDATED_AT, JsonPrimitive(updatedAt))
            if (isDisplayNameDirty) {
                put(COLUMN_DISPLAY_NAME, displayName?.let(::JsonPrimitive) ?: JsonNull)
            }
            if (isAvatarDirty) {
                put(COLUMN_AVATAR_URL, avatarUrl?.let(::JsonPrimitive) ?: JsonNull)
            }
        },
    )

    private companion object {
        const val SCHEMA = "public"
        const val TABLE = "user_data"
        const val COLUMN_ID = "id"
        const val COLUMN_DISPLAY_NAME = "display_name"
        const val COLUMN_AVATAR_URL = "avatar_url"
        const val COLUMN_UPDATED_AT = "updated_at"
        val COLUMNS = Columns.list(COLUMN_ID, COLUMN_DISPLAY_NAME, COLUMN_AVATAR_URL, COLUMN_UPDATED_AT)
    }
}
