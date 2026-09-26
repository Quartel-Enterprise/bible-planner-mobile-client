package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IsWholeChapterReadUseCaseTest {
    private lateinit var useCase: IsWholeChapterReadUseCase

    @BeforeTest
    fun setUp() {
        val database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.RUT,
            versesPerChapter = listOf(2, 2, 0),
            readVerses = mapOf(1 to setOf(1, 2), 2 to setOf(1)),
        )
        useCase = IsWholeChapterReadUseCase(
            chapterDao = database.chapterDao,
            verseDao = database.verseDao,
        )
    }

    @Test
    fun `GIVEN every verse read WHEN checking the chapter THEN is read`() = runTest {
        // When
        val result = useCase(
            chapterNumber = 1,
            bookId = BookId.RUT,
        )

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN an unread verse WHEN checking the chapter THEN is not read`() = runTest {
        // When
        val result = useCase(
            chapterNumber = 2,
            bookId = BookId.RUT,
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a chapter without verses WHEN checking it THEN is not read`() = runTest {
        // When
        val result = useCase(
            chapterNumber = 3,
            bookId = BookId.RUT,
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a chapter missing from the database WHEN checking it THEN is not read`() = runTest {
        // When
        val result = useCase(
            chapterNumber = 1,
            bookId = BookId.GEN,
        )

        // Then
        assertFalse(result)
    }
}
