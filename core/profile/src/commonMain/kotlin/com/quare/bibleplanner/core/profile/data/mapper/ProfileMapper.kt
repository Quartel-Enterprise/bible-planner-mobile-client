package com.quare.bibleplanner.core.profile.data.mapper

import com.quare.bibleplanner.core.profile.data.dto.ProfileDto
import com.quare.bibleplanner.core.profile.data.dto.ProfileRowDto
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import kotlin.time.Instant

internal class ProfileMapper {
    fun toDto(
        userId: String,
        entity: ProfileEntity,
    ): ProfileDto = ProfileDto(
        id = userId,
        displayName = entity.displayName,
        avatarUrl = entity.avatarUrl,
        updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt).toString(),
        isDisplayNameDirty = entity.displayNamePendingSync,
        isAvatarDirty = entity.avatarPendingSync && entity.pendingAvatarBytes == null,
    )

    fun toDto(row: ProfileRowDto): ProfileDto = ProfileDto(
        id = row.id,
        displayName = row.displayName,
        avatarUrl = row.avatarUrl,
        updatedAt = row.updatedAt,
        isDisplayNameDirty = false,
        isAvatarDirty = false,
    )

    fun toEpochMillis(updatedAt: String): Long = Instant.parse(updatedAt).toEpochMilliseconds()
}
