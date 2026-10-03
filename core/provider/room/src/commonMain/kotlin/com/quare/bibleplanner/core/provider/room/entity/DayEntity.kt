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
    // Why: stores ReadingPlanType.name (CHRONOLOGICAL or BOOKS); renaming an entry breaks stored rows.
    @ColumnInfo(defaultValue = "'BOOKS'") val readingPlanType: String,
    @ColumnInfo(defaultValue = "0") val isRead: Boolean,
    // Why: epoch milliseconds; null while the day is unread.
    val readTimestamp: Long?,
    val notes: String?,
    // Why: day-meta sync covers readTimestamp and notes only; isRead derives from chapter/verse state.
    @ColumnInfo(defaultValue = "NULL") val metaUpdatedAt: Long?,
    @ColumnInfo(defaultValue = "0") val isMetaPendingSync: Boolean,
)
