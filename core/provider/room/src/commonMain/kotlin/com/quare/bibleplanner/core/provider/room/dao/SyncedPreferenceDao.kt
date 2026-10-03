package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SyncedPreferenceDao {
    @Query("SELECT value FROM synced_preferences WHERE key = :key")
    abstract fun observeValue(key: String): Flow<String?>

    @Query("SELECT * FROM synced_preferences WHERE pendingSync = 1")
    abstract fun getPendingFlow(): Flow<List<SyncedPreferenceEntity>>

    @Query("SELECT * FROM synced_preferences WHERE pendingSync = 1")
    abstract suspend fun getPending(): List<SyncedPreferenceEntity>

    @Query(
        "INSERT OR REPLACE INTO synced_preferences (key, value, updatedAt, pendingSync) " +
            "VALUES (:key, :value, :updatedAt, 1)",
    )
    abstract suspend fun setLocal(
        key: String,
        value: String,
        updatedAt: Long,
    )

    // Why: the updatedAt guard keeps a change made while the push was in flight pending.
    @Query("UPDATE synced_preferences SET pendingSync = 0 WHERE key = :key AND updatedAt = :syncedUpdatedAt")
    abstract suspend fun markSynced(
        key: String,
        syncedUpdatedAt: Long,
    )

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

    // Why: updatedAt = 0 marks the row provisional, so any remote value wins over it and
    // adoptProvisional later promotes rows still at 0 to pending.
    @Query(
        "INSERT OR IGNORE INTO synced_preferences (key, value, updatedAt, pendingSync) " +
            "VALUES (:key, :value, 0, 0)",
    )
    abstract suspend fun seedProvisional(
        key: String,
        value: String,
    )

    @Query("UPDATE synced_preferences SET updatedAt = :now, pendingSync = 1 WHERE updatedAt = 0")
    abstract suspend fun adoptProvisional(now: Long)

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
