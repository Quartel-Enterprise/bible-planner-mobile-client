package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity

// Why: isSaved is set false instead of deleting the row (like a null highlight color)
// so an unsave reaches the other devices.
@Entity(
    tableName = "saved_verses",
    primaryKeys = ["bibleVersionId", "bookId", "chapterNumber", "verseNumber"],
)
data class SavedVerseEntity(
    val bibleVersionId: String,
    val bookId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val isSaved: Boolean,
    val updatedAtEpochMillis: Long,
    val isPendingSync: Boolean,
)
