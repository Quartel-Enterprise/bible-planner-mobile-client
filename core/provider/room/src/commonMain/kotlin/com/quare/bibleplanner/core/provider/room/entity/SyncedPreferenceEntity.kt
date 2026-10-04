package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/*
 * Why: Last-Write-Wins by updatedAt (epoch millis); pendingSync is set on local change and cleared
 * once pushed; a provisional local default uses updatedAt = 0 so any real remote value wins.
 */
@Entity(tableName = "synced_preferences")
data class SyncedPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long,
    val pendingSync: Boolean,
)
