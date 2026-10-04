package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/*
 * Why: the id is client-generated so a note created offline keeps its identity remotely.
 * Verses and version are payload, not identity (extending 3:1-3 to 3:1-5 keeps the note);
 * the version is indexed because a chapter is read in one version.
 */
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
