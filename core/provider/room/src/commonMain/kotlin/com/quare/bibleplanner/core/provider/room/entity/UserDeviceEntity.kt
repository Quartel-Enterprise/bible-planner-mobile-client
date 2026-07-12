package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "user_devices")
data class UserDeviceEntity(
    @PrimaryKey
    val id: String,
    val deviceId: String,
    val name: String,
    val platform: String,
    val formFactor: String,
    val locationCity: String?,
    val locationCountry: String?,
    val lastActiveAt: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val isNamePendingSync: Boolean,
)
