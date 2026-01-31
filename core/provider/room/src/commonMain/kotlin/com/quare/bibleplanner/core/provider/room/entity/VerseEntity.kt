package com.quare.bibleplanner.core.provider.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
)
