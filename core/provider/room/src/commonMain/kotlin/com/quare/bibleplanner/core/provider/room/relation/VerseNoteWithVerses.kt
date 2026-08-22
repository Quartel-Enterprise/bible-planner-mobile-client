package com.quare.bibleplanner.core.provider.room.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteVerseEntity

data class VerseNoteWithVerses(
    @Embedded val note: VerseNoteEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["noteId"])
    val verses: List<VerseNoteVerseEntity>,
)
