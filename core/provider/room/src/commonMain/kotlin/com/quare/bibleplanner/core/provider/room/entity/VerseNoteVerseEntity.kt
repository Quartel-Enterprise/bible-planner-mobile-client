package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    tableName = "verse_note_verses",
    primaryKeys = ["noteId", "verseNumber"],
    foreignKeys = [
        ForeignKey(
            entity = VerseNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("noteId")],
)
data class VerseNoteVerseEntity(
    val noteId: String,
    val verseNumber: Int,
    val position: Int,
)
