package com.quare.bibleplanner.core.provider.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Generic key-value store for synced scalar preferences. Mirrors the favorites sync mechanics
 * (pending flag + updatedAt timestamp, Last-Write-Wins) so any scalar setting can be synced by
 * adding a key — no new sync plumbing per setting.
 */
@Dao
abstract class SyncedPreferenceDao {
    @Query("SELECT value FROM synced_preferences WHERE key = :key")
    abstract fun observeValue(key: String): Flow<String?>

    @Query("SELECT * FROM synced_preferences WHERE pendingSync = 1")
    abstract fun getPendingFlow(): Flow<List<SyncedPreferenceEntity>>

    @Query("SELECT * FROM synced_preferences WHERE pendingSync = 1")
    abstract suspend fun getPending(): List<SyncedPreferenceEntity>

    /** Local write: upserts [value] and flags it pending so it gets pushed. */
    @Query(
        "INSERT OR REPLACE INTO synced_preferences (key, value, updatedAt, pendingSync) " +
            "VALUES (:key, :value, :updatedAt, 1)",
    )
    abstract suspend fun setLocal(
        key: String,
        value: String,
        updatedAt: Long,
    )

    /** Clears the pending flag only if the row was not re-touched meanwhile (timestamp guard). */
    @Query("UPDATE synced_preferences SET pendingSync = 0 WHERE key = :key AND updatedAt = :syncedUpdatedAt")
    abstract suspend fun markSynced(
        key: String,
        syncedUpdatedAt: Long,
    )

    /**
     * Applies a remote value with Last-Write-Wins: overwrites only a non-pending, strictly older row;
     * inserts when the row is absent. A pending or newer local row always wins (the echo of our own
     * write and stale remote rows are no-ops).
     */
    @Transaction
    open suspend fun applyRemote(
        key: String,
        value: String,
        remoteUpdatedAt: Long,
    ) {
        val updated = updateFromRemote(
            key = key,
            value = value,
            remoteUpdatedAt = remoteUpdatedAt,
        )
        if (updated == 0) {
            insertIfAbsent(
                SyncedPreferenceEntity(
                    key = key,
                    value = value,
                    updatedAt = remoteUpdatedAt,
                    pendingSync = false,
                ),
            )
        }
    }

    /** Seeds a provisional local default (non-pending, updatedAt = 0) only if the key is absent. */
    @Query(
        "INSERT OR IGNORE INTO synced_preferences (key, value, updatedAt, pendingSync) " +
            "VALUES (:key, :value, 0, 0)",
    )
    abstract suspend fun seedProvisional(
        key: String,
        value: String,
    )

    @Query("DELETE FROM synced_preferences WHERE key IN (:keys)")
    abstract suspend fun deleteByKeys(keys: List<String>)

    @Query("DELETE FROM synced_preferences")
    abstract suspend fun deleteAll()

    @Query(
        "UPDATE synced_preferences SET value = :value, updatedAt = :remoteUpdatedAt " +
            "WHERE key = :key AND pendingSync = 0 AND updatedAt < :remoteUpdatedAt",
    )
    protected abstract suspend fun updateFromRemote(
        key: String,
        value: String,
        remoteUpdatedAt: Long,
    ): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(entity: SyncedPreferenceEntity)
}
