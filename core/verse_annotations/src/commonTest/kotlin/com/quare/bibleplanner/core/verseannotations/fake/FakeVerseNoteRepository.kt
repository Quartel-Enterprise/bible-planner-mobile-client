package com.quare.bibleplanner.core.verseannotations.fake

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeVerseNoteRepository(
    initialNotes: List<VerseNote> = emptyList(),
) : VerseNoteRepository {
    val notes = MutableStateFlow(initialNotes)
    var deletedNoteIds: List<String> = emptyList()
        private set

    override fun observeChapterNotes(
        bookId: BookId,
        chapterNumber: Int,
    ): Flow<List<VerseNote>> = notes.map { current ->
        current.filter { it.bookId == bookId && it.chapterNumber == chapterNumber }
    }

    override suspend fun getNote(noteId: String): VerseNote? = notes.value.find { it.id == noteId }

    override suspend fun upsert(note: VerseNote) {
        notes.value = notes.value.filterNot { it.id == note.id } + note
    }

    override suspend fun delete(noteId: String) {
        deletedNoteIds = deletedNoteIds + noteId
        notes.value = notes.value.filterNot { it.id == noteId }
    }
}
