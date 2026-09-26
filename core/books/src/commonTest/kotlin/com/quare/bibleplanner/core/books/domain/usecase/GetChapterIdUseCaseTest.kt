package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetChapterIdUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: GetChapterIdUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(1, 1),
        )
        useCase = GetChapterIdUseCase(database.chapterDao)
    }

    @Test
    fun `GIVEN a stored chapter WHEN looking it up THEN returns its id`() = runTest {
        // When
        val result = useCase(
            bookId = BookId.GEN,
            chapterNumber = 2,
        )

        // Then
        assertEquals(
            database
                .chapter(
                    bookId = BookId.GEN,
                    chapterNumber = 2,
                ).id,
            result,
        )
    }

    @Test
    fun `GIVEN a missing chapter WHEN looking it up THEN returns null`() = runTest {
        // When
        val result = useCase(
            bookId = BookId.EXO,
            chapterNumber = 1,
        )

        // Then
        assertNull(result)
    }
}
