package com.quare.bibleplanner.core.provider.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "days",
    indices = [Index(value = ["weekNumber", "dayNumber", "readingPlanType"], unique = true)],
)
data class DayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekNumber: Int,
    val dayNumber: Int,
    val readingPlanType: String, // CHRONOLOGICAL or BOOKS
    val isRead: Boolean = false,
    val readTimestamp: Long? = null, // Epoch milliseconds, null if not read
    val notes: String? = null,
)
