package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity

@Entity(
    tableName = "verse_highlights",
    primaryKeys = ["bibleVersionId", "bookId", "chapterNumber", "verseNumber"],
)
data class VerseHighlightEntity(
    val bibleVersionId: String,
    val bookId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val color: String?,
    val updatedAtEpochMillis: Long,
    val isPendingSync: Boolean,
)
