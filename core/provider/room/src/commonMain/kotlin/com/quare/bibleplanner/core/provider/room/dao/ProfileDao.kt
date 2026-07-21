package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = :id")
    abstract fun observeProfile(id: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE displayNamePendingSync = 1 OR avatarPendingSync = 1")
    abstract fun getPendingFlow(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE displayNamePendingSync = 1 OR avatarPendingSync = 1")
    abstract suspend fun getPending(): List<ProfileEntity>

    @Transaction
    open suspend fun setDisplayNameLocal(
        id: String,
        displayName: String?,
        updatedAt: Long,
    ) {
        val updated = updateDisplayNameLocal(
            id = id,
            displayName = displayName,
            updatedAt = updatedAt,
        )
        if (updated == 0) {
            insertIfAbsent(
                ProfileEntity(
                    id = id,
                    displayName = displayName,
                    avatarUrl = null,
                    pendingAvatarBytes = null,
                    updatedAt = updatedAt,
                    displayNamePendingSync = true,
                    avatarPendingSync = false,
                ),
            )
        }
    }

    @Transaction
    open suspend fun setAvatarLocal(
        id: String,
        avatarUrl: String?,
        pendingAvatarBytes: ByteArray?,
        updatedAt: Long,
    ) {
        val updated = updateAvatarLocal(
            id = id,
            avatarUrl = avatarUrl,
            pendingAvatarBytes = pendingAvatarBytes,
            updatedAt = updatedAt,
        )
        if (updated == 0) {
            insertIfAbsent(
                ProfileEntity(
                    id = id,
                    displayName = null,
                    avatarUrl = avatarUrl,
                    pendingAvatarBytes = pendingAvatarBytes,
                    updatedAt = updatedAt,
                    displayNamePendingSync = false,
                    avatarPendingSync = true,
                ),
            )
        }
    }

    @Query(
        "UPDATE profiles SET " +
            "displayNamePendingSync = CASE WHEN :clearDisplayName THEN 0 ELSE displayNamePendingSync END, " +
            "avatarPendingSync = CASE WHEN :clearAvatar THEN 0 ELSE avatarPendingSync END " +
            "WHERE id = :id AND updatedAt = :syncedUpdatedAt",
    )
    abstract suspend fun markSynced(
        id: String,
        syncedUpdatedAt: Long,
        clearDisplayName: Boolean,
        clearAvatar: Boolean,
    )

    @Query(
        "UPDATE profiles SET avatarUrl = :avatarUrl, pendingAvatarBytes = NULL, avatarPendingSync = 1 " +
            "WHERE id = :id AND updatedAt = :pendingUpdatedAt",
    )
    abstract suspend fun onAvatarUploaded(
        id: String,
        avatarUrl: String,
        pendingUpdatedAt: Long,
    )

    @Transaction
    open suspend fun applyRemote(
        id: String,
        displayName: String?,
        avatarUrl: String?,
        remoteUpdatedAt: Long,
    ) {
        val updated = updateFromRemote(
            id = id,
            displayName = displayName,
            avatarUrl = avatarUrl,
            remoteUpdatedAt = remoteUpdatedAt,
        )
        if (updated == 0) {
            insertIfAbsent(
                ProfileEntity(
                    id = id,
                    displayName = displayName,
                    avatarUrl = avatarUrl,
                    pendingAvatarBytes = null,
                    updatedAt = remoteUpdatedAt,
                    displayNamePendingSync = false,
                    avatarPendingSync = false,
                ),
            )
        }
    }

    @Query("DELETE FROM profiles")
    abstract suspend fun deleteAll()

    @Query(
        "UPDATE profiles SET displayName = :displayName, updatedAt = :updatedAt, " +
            "displayNamePendingSync = 1 WHERE id = :id",
    )
    protected abstract suspend fun updateDisplayNameLocal(
        id: String,
        displayName: String?,
        updatedAt: Long,
    ): Int

    @Query(
        "UPDATE profiles SET avatarUrl = :avatarUrl, pendingAvatarBytes = :pendingAvatarBytes, " +
            "updatedAt = :updatedAt, avatarPendingSync = 1 WHERE id = :id",
    )
    protected abstract suspend fun updateAvatarLocal(
        id: String,
        avatarUrl: String?,
        pendingAvatarBytes: ByteArray?,
        updatedAt: Long,
    ): Int

    @Query(
        "UPDATE profiles SET displayName = :displayName, avatarUrl = :avatarUrl, " +
            "pendingAvatarBytes = NULL, updatedAt = :remoteUpdatedAt " +
            "WHERE id = :id AND displayNamePendingSync = 0 AND avatarPendingSync = 0 " +
            "AND updatedAt < :remoteUpdatedAt",
    )
    protected abstract suspend fun updateFromRemote(
        id: String,
        displayName: String?,
        avatarUrl: String?,
        remoteUpdatedAt: Long,
    ): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(entity: ProfileEntity)
}
