package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity

// Why: keyed by canonical verse coordinates, not VerseEntity.id, to survive version re-downloads
// and match the backend PK; version is part of it since translations differ. A null color is
// a removal tombstone with a fresh updatedAtEpochMillis: the sync engine never deletes rows.
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
