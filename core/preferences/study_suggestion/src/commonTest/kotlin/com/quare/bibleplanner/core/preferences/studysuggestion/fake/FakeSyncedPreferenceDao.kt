package com.quare.bibleplanner.core.preferences.studysuggestion.fake

import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeSyncedPreferenceDao(
    initialValues: Map<String, String>,
) : SyncedPreferenceDao() {
    private val values = MutableStateFlow(initialValues)
    val localWrites = mutableListOf<SyncedPreferenceEntity>()

    override fun observeValue(key: String): Flow<String?> = values.map { current -> current[key] }

    override suspend fun setLocal(
        key: String,
        value: String,
        updatedAt: Long,
    ) {
        localWrites += SyncedPreferenceEntity(
            key = key,
            value = value,
            updatedAt = updatedAt,
            pendingSync = true,
        )
        values.value += key to value
    }

    override fun getPendingFlow(): Flow<List<SyncedPreferenceEntity>> = error("unused")

    override suspend fun getPending(): List<SyncedPreferenceEntity> = error("unused")

    override suspend fun markSynced(
        key: String,
        syncedUpdatedAt: Long,
    ) = error("unused")

    override suspend fun seedProvisional(
        key: String,
        value: String,
    ) = error("unused")

    override suspend fun adoptProvisional(now: Long) = error("unused")

    override suspend fun deleteByKeys(keys: List<String>) = error("unused")

    override suspend fun deleteAll() = error("unused")

    override suspend fun updateFromRemote(
        key: String,
        value: String,
        remoteUpdatedAt: Long,
    ): Int = error("unused")

    override suspend fun insertIfAbsent(entity: SyncedPreferenceEntity) = error("unused")
}
