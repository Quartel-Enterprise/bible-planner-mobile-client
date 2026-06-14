package com.quare.bibleplanner.core.provider.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["bookId"])],
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: Int,
    val bookId: String,
    val isRead: Boolean = false,
    // Read-state sync metadata (Last-Write-Wins by readUpdatedAt; pending flag drives the push loop).
    @ColumnInfo(defaultValue = "NULL") val readUpdatedAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val isReadPendingSync: Boolean = false,
)
