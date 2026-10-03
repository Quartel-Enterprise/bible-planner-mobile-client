package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "days",
    indices = [Index(value = ["weekNumber", "dayNumber", "readingPlanType"], unique = true)],
)
data class DayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val weekNumber: Int,
    val dayNumber: Int,
    @ColumnInfo(defaultValue = "'BOOKS'") val readingPlanType: String, // CHRONOLOGICAL or BOOKS
    @ColumnInfo(defaultValue = "0") val isRead: Boolean,
    val readTimestamp: Long?, // Epoch milliseconds, null if not read
    val notes: String?,
    // Day-meta sync metadata (readTimestamp + notes; isRead itself derives from chapter/verse state).
    @ColumnInfo(defaultValue = "NULL") val metaUpdatedAt: Long?,
    @ColumnInfo(defaultValue = "0") val isMetaPendingSync: Boolean,
)
