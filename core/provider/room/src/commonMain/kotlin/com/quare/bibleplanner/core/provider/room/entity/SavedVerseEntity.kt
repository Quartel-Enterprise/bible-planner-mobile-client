package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity

/**
 * A verse the user bookmarked. [isSaved] is false rather than the row being deleted for the same
 * reason [VerseHighlightEntity.color] is nullable: an unsave has to reach the other devices.
 */
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
