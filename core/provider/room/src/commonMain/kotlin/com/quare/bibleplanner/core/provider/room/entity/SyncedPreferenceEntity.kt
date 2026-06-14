package com.quare.bibleplanner.core.provider.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single user-scoped scalar setting kept in sync with the backend (Last-Write-Wins by
 * [updatedAt]). Generic home for any synced scalar preference — the [key] identifies the setting
 * (e.g. `plan_start_date`), [value] holds its string-encoded value.
 *
 * - [pendingSync] is set when the value is changed locally and cleared once pushed.
 * - [updatedAt] is the epoch-millis timestamp of the last local or remote change; a provisional
 *   local default uses `0` so any real remote value wins.
 */
@Entity(tableName = "synced_preferences")
data class SyncedPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long,
    val pendingSync: Boolean,
)
