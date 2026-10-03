package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "synced_preferences")
data class SyncedPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long,
    val pendingSync: Boolean,
)
