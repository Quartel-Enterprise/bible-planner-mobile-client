package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.DeleteVerseNoteUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.GetVerseNoteUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseNoteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class VerseNoteUseCasesTest {
    private val note = VerseNote(
        id = "note-1",
        chapter = ChapterRef(
            bibleVersionId = "ACF",
            bookId = BookId.GEN,
            chapterNumber = 1,
        ),
        verseNumbers = listOf(1),
        text = "In the beginning",
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
    )
    private lateinit var repository: FakeVerseNoteRepository
    private lateinit var getVerseNote: GetVerseNoteUseCase
    private lateinit var deleteVerseNote: DeleteVerseNoteUseCase

    @BeforeTest
    fun setUp() {
        repository = FakeVerseNoteRepository(initialNotes = listOf(note))
        getVerseNote = GetVerseNoteUseCase(repository)
        deleteVerseNote = DeleteVerseNoteUseCase(repository)
    }

    @Test
    fun `gets a stored note by its id`() = runTest {
        // When
        val found = getVerseNote("note-1")

        // Then
        assertEquals(
            expected = note,
            actual = found,
        )
    }

    @Test
    fun `a deleted note can no longer be found`() = runTest {
        // When
        deleteVerseNote("note-1")

        // Then
        assertEquals(
            expected = listOf("note-1"),
            actual = repository.deletedNoteIds,
        )
        assertNull(getVerseNote("note-1"))
    }
}
