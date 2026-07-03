package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "verses",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["chapterId"]),
        Index(value = ["chapterId", "number"], unique = true),
    ],
)
data class VerseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val number: Int,
    val chapterId: Long,
    val isRead: Boolean = false,
    // Read-state sync metadata, only populated for verse-range reads (whole-chapter reads sync at chapter level).
    @ColumnInfo(defaultValue = "NULL") val readUpdatedAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val isReadPendingSync: Boolean = false,
)
