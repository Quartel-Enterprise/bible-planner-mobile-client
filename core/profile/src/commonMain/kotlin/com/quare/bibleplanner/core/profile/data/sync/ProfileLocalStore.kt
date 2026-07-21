package com.quare.bibleplanner.core.profile.data.sync

import com.quare.bibleplanner.core.profile.data.dto.ProfileDto
import com.quare.bibleplanner.core.profile.data.mapper.ProfileMapper
import com.quare.bibleplanner.core.provider.room.dao.ProfileDao
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import kotlinx.coroutines.flow.Flow

internal class ProfileLocalStore(
    private val profileDao: ProfileDao,
    private val profileMapper: ProfileMapper,
) : SyncLocalStore<ProfileEntity, ProfileDto> {
    override fun pendingFlow(): Flow<List<ProfileEntity>> = profileDao.getPendingFlow()

    override suspend fun getPending(): List<ProfileEntity> = profileDao.getPending()

    override suspend fun markSynced(entity: ProfileEntity) {
        profileDao.markSynced(
            id = entity.id,
            syncedUpdatedAt = entity.updatedAt,
            clearDisplayName = entity.displayNamePendingSync,
            clearAvatar = entity.avatarPendingSync && entity.pendingAvatarBytes == null,
        )
    }

    override suspend fun applyRemote(dto: ProfileDto) {
        profileDao.applyRemote(
            id = dto.id,
            displayName = dto.displayName,
            avatarUrl = dto.avatarUrl,
            remoteUpdatedAt = profileMapper.toEpochMillis(dto.updatedAt),
        )
    }

    override fun toDto(
        userId: String,
        entity: ProfileEntity,
    ): ProfileDto = profileMapper.toDto(
        userId = userId,
        entity = entity,
    )

    override suspend fun clearLocal() {
        profileDao.deleteAll()
    }
}
