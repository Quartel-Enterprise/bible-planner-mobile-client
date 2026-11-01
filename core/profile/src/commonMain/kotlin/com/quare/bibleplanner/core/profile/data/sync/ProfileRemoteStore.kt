package com.quare.bibleplanner.core.profile.data.sync

import com.quare.bibleplanner.core.profile.data.dto.ProfileDto
import com.quare.bibleplanner.core.profile.data.dto.ProfileRowDto
import com.quare.bibleplanner.core.profile.data.mapper.ProfileMapper
import com.quare.bibleplanner.core.provider.supabase.decodeUpsert
import com.quare.bibleplanner.core.provider.supabase.observeUserRows
import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal class ProfileRemoteStore(
    private val supabaseClient: SupabaseClient,
    private val realtime: Realtime,
    private val profileMapper: ProfileMapper,
) : SyncRemoteStore<ProfileDto> {
    private val columns = Columns.list(COLUMN_ID, COLUMN_DISPLAY_NAME, COLUMN_AVATAR_URL, COLUMN_UPDATED_AT)

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
        .select(columns = columns) {
            filter { eq(COLUMN_ID, userId) }
        }.decodeList<ProfileRowDto>()
        .map(profileMapper::toDto)

    override fun observeRemote(userId: String): Flow<ProfileDto> = realtime.observeUserRows(
        table = TABLE,
        userId = userId,
        userIdColumn = COLUMN_ID,
        upserted = { action -> action.decodeUpsert<ProfileRowDto>()?.let(profileMapper::toDto) },
        deleted = null,
    )

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
        const val TABLE = "user_data"
        const val COLUMN_ID = "id"
        const val COLUMN_DISPLAY_NAME = "display_name"
        const val COLUMN_AVATAR_URL = "avatar_url"
        const val COLUMN_UPDATED_AT = "updated_at"
    }
}
