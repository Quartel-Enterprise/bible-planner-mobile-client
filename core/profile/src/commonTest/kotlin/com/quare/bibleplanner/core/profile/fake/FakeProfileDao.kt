package com.quare.bibleplanner.core.profile.fake

import com.quare.bibleplanner.core.provider.room.dao.ProfileDao
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeProfileDao(
    initialRows: List<ProfileEntity> = emptyList(),
) : ProfileDao() {
    val rows = MutableStateFlow(initialRows)

    override fun observeProfile(id: String): Flow<ProfileEntity?> = rows.map { current -> current.find { it.id == id } }

    override fun getPendingFlow(): Flow<List<ProfileEntity>> = rows.map { current -> current.filter { it.isPending() } }

    override suspend fun getPending(): List<ProfileEntity> = rows.value.filter { it.isPending() }

    override suspend fun markSynced(
        id: String,
        syncedUpdatedAt: Long,
        clearDisplayName: Boolean,
        clearAvatar: Boolean,
    ) {
        updateRows(id) { row ->
            if (row.updatedAt == syncedUpdatedAt) {
                row.copy(
                    displayNamePendingSync = row.displayNamePendingSync && !clearDisplayName,
                    avatarPendingSync = row.avatarPendingSync && !clearAvatar,
                )
            } else {
                row
            }
        }
    }

    override suspend fun onAvatarUploaded(
        id: String,
        avatarUrl: String,
        pendingUpdatedAt: Long,
    ) {
        updateRows(id) { row ->
            if (row.updatedAt == pendingUpdatedAt) {
                row.copy(
                    avatarUrl = avatarUrl,
                    pendingAvatarBytes = null,
                    avatarPendingSync = true,
                )
            } else {
                row
            }
        }
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }

    override suspend fun updateDisplayNameLocal(
        id: String,
        displayName: String?,
        updatedAt: Long,
    ): Int = updateRows(id) { row ->
        row.copy(
            displayName = displayName,
            updatedAt = updatedAt,
            displayNamePendingSync = true,
        )
    }

    override suspend fun updateAvatarLocal(
        id: String,
        avatarUrl: String?,
        pendingAvatarBytes: ByteArray?,
        updatedAt: Long,
    ): Int = updateRows(id) { row ->
        row.copy(
            avatarUrl = avatarUrl,
            pendingAvatarBytes = pendingAvatarBytes,
            updatedAt = updatedAt,
            avatarPendingSync = true,
        )
    }

    override suspend fun updateFromRemote(
        id: String,
        displayName: String?,
        avatarUrl: String?,
        remoteUpdatedAt: Long,
    ): Int = updateRows(id) { row ->
        if (!row.isPending() && row.updatedAt < remoteUpdatedAt) {
            row.copy(
                displayName = displayName,
                avatarUrl = avatarUrl,
                pendingAvatarBytes = null,
                updatedAt = remoteUpdatedAt,
            )
        } else {
            row
        }
    }

    override suspend fun insertIfAbsent(entity: ProfileEntity) {
        if (rows.value.none { it.id == entity.id }) rows.value = rows.value + entity
    }

    private fun updateRows(
        id: String,
        transform: (ProfileEntity) -> ProfileEntity,
    ): Int {
        var changedRows = 0
        rows.value = rows.value.map { row ->
            if (row.id == id) {
                transform(row).also { updated -> if (updated != row) changedRows++ }
            } else {
                row
            }
        }
        return changedRows
    }

    private fun ProfileEntity.isPending(): Boolean = displayNamePendingSync || avatarPendingSync
}
