package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDeviceDao {
    @Query("SELECT * FROM user_devices")
    fun observeAll(): Flow<List<UserDeviceEntity>>

    @Query("SELECT * FROM user_devices WHERE id = :id")
    suspend fun getById(id: String): UserDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: UserDeviceEntity)

    // Local rename → marks the row pending; the sync engine pushes it while online.
    @Query("UPDATE user_devices SET name = :name, updatedAt = :now, isNamePendingSync = 1 WHERE id = :id")
    suspend fun renameLocal(
        id: String,
        name: String,
        now: Long,
    )

    @Query("SELECT * FROM user_devices WHERE isNamePendingSync = 1")
    fun getPendingFlow(): Flow<List<UserDeviceEntity>>

    @Query("SELECT * FROM user_devices WHERE isNamePendingSync = 1")
    suspend fun getPending(): List<UserDeviceEntity>

    // Clears pending only if the row was not renamed again after the push started (LWW guard).
    @Query("UPDATE user_devices SET isNamePendingSync = 0 WHERE id = :id AND updatedAt = :syncedUpdatedAt")
    suspend fun markNameSynced(
        id: String,
        syncedUpdatedAt: Long,
    )

    // Server-authoritative fields are always applied from the remote row.
    @Query(
        "UPDATE user_devices SET deviceId = :deviceId, platform = :platform, formFactor = :formFactor, " +
            "locationCity = :locationCity, locationCountry = :locationCountry, lastActiveAt = :lastActiveAt " +
            "WHERE id = :id",
    )
    suspend fun applyRemoteServerFields(
        id: String,
        deviceId: String,
        platform: String,
        formFactor: String,
        locationCity: String?,
        locationCountry: String?,
        lastActiveAt: Long,
    )

    // Name is overwritten by the remote only when the local row is not pending and strictly older (LWW).
    @Query(
        "UPDATE user_devices SET name = :name, updatedAt = :remoteUpdatedAt " +
            "WHERE id = :id AND isNamePendingSync = 0 AND updatedAt < :remoteUpdatedAt",
    )
    suspend fun applyRemoteName(
        id: String,
        name: String,
        remoteUpdatedAt: Long,
    )

    @Query("DELETE FROM user_devices WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM user_devices WHERE id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM user_devices")
    suspend fun deleteAll()
}
