package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class ClearLocalReadingDataUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: ClearLocalReadingDataUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(1),
            isBookRead = true,
        )
        database.seedBook(
            bookId = BookId.EXO,
            versesPerChapter = listOf(1),
            isBookRead = true,
        )
        useCase = ClearLocalReadingDataUseCase(database.bookDao)
    }

    @Test
    fun `GIVEN read books WHEN clearing the local reading data THEN resets their read flag but keeps the rows`() =
        runTest {
            // When
            useCase()

            // Then
            assertEquals(listOf("GEN", "EXO"), database.books.map { it.id })
            assertFalse(database.books.any { it.isRead })
        }
}
