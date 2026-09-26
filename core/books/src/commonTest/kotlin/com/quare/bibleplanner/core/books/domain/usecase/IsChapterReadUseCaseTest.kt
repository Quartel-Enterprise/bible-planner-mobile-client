package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IsChapterReadUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: IsChapterReadUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(5, 3),
            readChapters = setOf(2),
            readVerses = mapOf(1 to setOf(1, 2, 3)),
        )
        useCase = IsChapterReadUseCase(
            chapterDao = database.chapterDao,
            verseDao = database.verseDao,
            isWholeChapterRead = IsWholeChapterReadUseCase(
                chapterDao = database.chapterDao,
                verseDao = database.verseDao,
            ),
        )
    }

    @Test
    fun `GIVEN an unknown chapter WHEN checking THEN is not read`() = runTest {
        // When
        val result = useCase(chapter(number = 9))

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN every verse of the range read WHEN checking the range THEN is read`() = runTest {
        // When
        val result = useCase(
            chapter(
                number = 1,
                startVerse = 1,
                endVerse = 3,
            ),
        )

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN one unread verse in the range WHEN checking the range THEN is not read`() = runTest {
        // When
        val result = useCase(
            chapter(
                number = 1,
                startVerse = 2,
                endVerse = 4,
            ),
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN unread verses after the start verse WHEN checking from the start verse THEN is not read`() = runTest {
        // When
        val result = useCase(
            chapter(
                number = 1,
                startVerse = 3,
            ),
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a chapter flagged read WHEN checking from a start verse THEN follows the verse flags`() = runTest {
        // When
        val result = useCase(
            chapter(
                number = 2,
                startVerse = 1,
            ),
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a chapter flagged read WHEN checking the whole chapter THEN is read`() = runTest {
        // When
        val result = useCase(chapter(number = 2))

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a partially read chapter WHEN checking the whole chapter THEN is not read`() = runTest {
        // When
        val result = useCase(chapter(number = 1))

        // Then
        assertFalse(result)
    }

    private fun chapter(
        number: Int,
        startVerse: Int? = null,
        endVerse: Int? = null,
    ): ChapterModel = ChapterModel(
        number = number,
        startVerse = startVerse,
        endVerse = endVerse,
        bookId = BookId.GEN,
    )
}
