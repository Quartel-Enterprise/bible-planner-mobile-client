package com.quare.bibleplanner.core.plan.fake

import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeSyncedPreferenceDao : SyncedPreferenceDao() {
    val rows = MutableStateFlow<Map<String, SyncedPreferenceEntity>>(emptyMap())
    val calls = mutableListOf<String>()

    override fun observeValue(key: String): Flow<String?> = rows.map { it[key]?.value }

    override fun getPendingFlow(): Flow<List<SyncedPreferenceEntity>> = rows.map(::pendingOf)

    override suspend fun getPending(): List<SyncedPreferenceEntity> = pendingOf(rows.value)

    override suspend fun setLocal(
        key: String,
        value: String,
        updatedAt: Long,
    ) {
        put(
            SyncedPreferenceEntity(
                key = key,
                value = value,
                updatedAt = updatedAt,
                pendingSync = true,
            ),
        )
    }

    override suspend fun markSynced(
        key: String,
        syncedUpdatedAt: Long,
    ) {
        calls += "markSynced($key, $syncedUpdatedAt)"
    }

    override suspend fun seedProvisional(
        key: String,
        value: String,
    ) {
        if (key !in rows.value) {
            put(
                SyncedPreferenceEntity(
                    key = key,
                    value = value,
                    updatedAt = 0L,
                    pendingSync = false,
                ),
            )
        }
    }

    override suspend fun adoptProvisional(now: Long) {
        calls += "adoptProvisional($now)"
    }

    override suspend fun deleteByKeys(keys: List<String>) {
        rows.value -= keys.toSet()
    }

    override suspend fun deleteAll() {
        calls += "deleteAll"
        rows.value = emptyMap()
    }

    override suspend fun updateFromRemote(
        key: String,
        value: String,
        remoteUpdatedAt: Long,
    ): Int {
        val current = rows.value[key] ?: return 0
        if (current.pendingSync || current.updatedAt >= remoteUpdatedAt) return 0
        put(
            current.copy(
                value = value,
                updatedAt = remoteUpdatedAt,
            ),
        )
        return 1
    }

    override suspend fun insertIfAbsent(entity: SyncedPreferenceEntity) {
        if (entity.key !in rows.value) put(entity)
    }

    private fun put(entity: SyncedPreferenceEntity) {
        rows.value += entity.key to entity
    }

    private fun pendingOf(current: Map<String, SyncedPreferenceEntity>): List<SyncedPreferenceEntity> =
        current.values.filter { it.pendingSync }
}
