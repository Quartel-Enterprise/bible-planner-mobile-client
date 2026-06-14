package com.quare.bibleplanner.core.plan.data.mapper

import com.quare.bibleplanner.core.plan.data.dto.UserPreferenceDto
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlin.time.Instant

internal class UserPreferenceMapper {
    fun toDto(
        userId: String,
        entity: SyncedPreferenceEntity,
    ): UserPreferenceDto = UserPreferenceDto(
        userId = userId,
        key = entity.key,
        value = entity.value,
        updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt).toString(),
    )

    fun toEpochMillis(updatedAt: String): Long = Instant.parse(updatedAt).toEpochMilliseconds()
}
