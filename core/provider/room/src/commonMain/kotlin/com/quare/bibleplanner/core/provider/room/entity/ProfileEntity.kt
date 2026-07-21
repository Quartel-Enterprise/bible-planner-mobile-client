package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val displayName: String?,
    val avatarUrl: String?,
    val pendingAvatarBytes: ByteArray?,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val displayNamePendingSync: Boolean,
    @ColumnInfo(defaultValue = "0") val avatarPendingSync: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfileEntity) return false
        return id == other.id &&
            displayName == other.displayName &&
            avatarUrl == other.avatarUrl &&
            pendingAvatarBytes.contentEquals(other.pendingAvatarBytes) &&
            updatedAt == other.updatedAt &&
            displayNamePendingSync == other.displayNamePendingSync &&
            avatarPendingSync == other.avatarPendingSync
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = HASH_FACTOR * result + displayName.hashCode()
        result = HASH_FACTOR * result + avatarUrl.hashCode()
        result = HASH_FACTOR * result + pendingAvatarBytes.contentHashCode()
        result = HASH_FACTOR * result + updatedAt.hashCode()
        result = HASH_FACTOR * result + displayNamePendingSync.hashCode()
        result = HASH_FACTOR * result + avatarPendingSync.hashCode()
        return result
    }

    private companion object {
        const val HASH_FACTOR = 31
    }
}
