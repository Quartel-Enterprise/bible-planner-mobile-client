package com.quare.bibleplanner.core.plan.data.sync

import com.quare.bibleplanner.core.plan.data.dto.UserPreferenceDto
import com.quare.bibleplanner.core.plan.data.mapper.UserPreferenceMapper
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import kotlinx.coroutines.flow.Flow

/**
 * Adapts the generic `synced_preferences` table to the sync engine. This drives the whole scalar
 * key-value store (currently the reading-plan preferences; future scalar settings just add a key and
 * are synced through here automatically). The one-time DataStore→Room migration runs at startup, not
 * here, so logged-out existing users keep their data before the session-scoped sync starts.
 */
internal class SyncedPreferenceLocalStore(
    private val dao: SyncedPreferenceDao,
    private val mapper: UserPreferenceMapper,
) : SyncLocalStore<SyncedPreferenceEntity, UserPreferenceDto> {
    override fun pendingFlow(): Flow<List<SyncedPreferenceEntity>> = dao.getPendingFlow()

    override suspend fun getPending(): List<SyncedPreferenceEntity> = dao.getPending()

    override suspend fun markSynced(entity: SyncedPreferenceEntity) {
        dao.markSynced(
            key = entity.key,
            syncedUpdatedAt = entity.updatedAt,
        )
    }

    override suspend fun applyRemote(dto: UserPreferenceDto) {
        dao.applyRemote(
            key = dto.key,
            value = dto.value,
            remoteUpdatedAt = mapper.toEpochMillis(dto.updatedAt),
        )
    }

    override fun toDto(
        userId: String,
        entity: SyncedPreferenceEntity,
    ): UserPreferenceDto = mapper.toDto(
        userId = userId,
        entity = entity,
    )

    override suspend fun clearLocal() {
        dao.deleteAll()
    }
}
