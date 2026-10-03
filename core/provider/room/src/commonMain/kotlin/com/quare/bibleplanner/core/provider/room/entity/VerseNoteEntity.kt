package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "verse_notes",
    indices = [Index("bibleVersionId", "bookId", "chapterNumber")],
)
data class VerseNoteEntity(
    @PrimaryKey val id: String,
    val bibleVersionId: String,
    val bookId: String,
    val chapterNumber: Int,
    val text: String,
    val isDeleted: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isPendingSync: Boolean,
)
