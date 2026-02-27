package com.quare.bibleplanner.core.provider.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "verse_texts",
    foreignKeys = [
        ForeignKey(
            entity = VerseEntity::class,
            parentColumns = ["id"],
            childColumns = ["verseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BibleVersionEntity::class,
            parentColumns = ["id"],
            childColumns = ["bibleVersionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["verseId"]),
        Index(value = ["bibleVersionId"]),
        Index(value = ["verseId", "bibleVersionId"], unique = true),
    ],
)
data class VerseTextEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val verseId: Long,
    val bibleVersionId: String,
    val text: String,
)
