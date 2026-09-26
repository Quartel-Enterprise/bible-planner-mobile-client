package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeBooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BooksQueriesUseCasesTest {
    private val genesis = book(BookId.GEN)
    private val exodus = book(BookId.EXO)

    private lateinit var repository: FakeBooksRepository

    @Test
    fun `GIVEN stored books WHEN observing the books THEN emits them`() = runTest {
        // Given
        prepareScenario(books = listOf(genesis, exodus))

        // When
        val books = GetBooksFlowUseCase(repository)().first()

        // Then
        assertEquals(listOf(genesis, exodus), books)
    }

    @Test
    fun `GIVEN stored books WHEN observing one by id THEN emits only that book`() = runTest {
        // Given
        prepareScenario(books = listOf(genesis, exodus))

        // When
        val book = GetBookByIdFlowUseCase(repository)(BookId.EXO).first()

        // Then
        assertEquals(exodus, book)
    }

    @Test
    fun `GIVEN a book that is not stored WHEN observing it by id THEN emits null`() = runTest {
        // Given
        prepareScenario(books = listOf(genesis))

        // When
        val book = GetBookByIdFlowUseCase(repository)(BookId.REV).first()

        // Then
        assertNull(book)
    }

    @Test
    fun `GIVEN no stored books WHEN initializing if needed THEN initializes the database`() = runTest {
        // Given
        prepareScenario(books = emptyList())

        // When
        InitializeBooksIfNeededUseCase(repository)()

        // Then
        assertEquals(1, repository.initializationCount)
    }

    @Test
    fun `GIVEN stored books WHEN initializing if needed THEN skips the initialization`() = runTest {
        // Given
        prepareScenario(books = listOf(genesis))

        // When
        InitializeBooksIfNeededUseCase(repository)()

        // Then
        assertEquals(0, repository.initializationCount)
    }

    @Test
    fun `GIVEN books and stored preferences WHEN observing them together THEN combines the three`() = runTest {
        // Given
        prepareScenario(books = listOf(genesis))
        repository.setBookLayoutFormat("list")
        repository.setSelectedTestament("old")

        // When
        val result = GetBooksWithInformationBoxVisibilityUseCase(repository)().first()

        // Then
        assertEquals(
            GetBooksWithInformationBoxVisibilityUseCase.Result(
                books = listOf(genesis),
                layoutFormat = "list",
                selectedTestament = "old",
            ),
            result,
        )
    }

    private fun book(bookId: BookId): BookDataModel = BookDataModel(
        id = bookId,
        chapters = emptyList(),
        isRead = false,
        isFavorite = false,
    )

    private fun prepareScenario(books: List<BookDataModel>) {
        repository = FakeBooksRepository(books)
    }
}
