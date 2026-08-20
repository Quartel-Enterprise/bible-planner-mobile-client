package com.quare.bibleplanner.core.verseannotations.domain.repository

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import kotlinx.coroutines.flow.Flow

interface VerseNoteRepository {
    fun observeChapterNotes(chapter: ChapterRef): Flow<List<VerseNote>>

    suspend fun getNote(noteId: String): VerseNote?

    suspend fun upsert(note: VerseNote)

    suspend fun delete(noteId: String)
}
