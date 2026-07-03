package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

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
