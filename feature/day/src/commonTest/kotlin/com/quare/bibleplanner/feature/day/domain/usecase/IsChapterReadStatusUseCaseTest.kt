package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetBooksFlowUseCase
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import com.quare.bibleplanner.feature.day.fake.FakeBooksRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class IsChapterReadStatusUseCaseTest {
    private val passage = PassageModel(
        bookId = BookId.GEN,
        chapters = listOf(
            ChapterModel(
                number = 1,
                startVerse = null,
                endVerse = null,
                bookId = BookId.GEN,
            ),
            ChapterModel(
                number = 2,
                startVerse = null,
                endVerse = null,
                bookId = BookId.GEN,
            ),
        ),
        isRead = true,
        chapterRanges = "1-2",
    )
    private lateinit var useCase: IsChapterReadStatusUseCase

    @Test
    fun `GIVEN a read chapter WHEN toggling it THEN the new status is unread`() = runTest {
        // Given
        prepareScenario()

        // When
        val result = useCase(
            passage = passage,
            strategy = UpdateReadStatusOfPassageStrategy.Chapter(
                passageIndex = 0,
                chapterIndex = 0,
            ),
        )

        // Then
        assertEquals(false, result.getOrNull())
    }

    @Test
    fun `GIVEN an unread chapter WHEN toggling it THEN the new status is read`() = runTest {
        // Given
        prepareScenario()

        // When
        val result = useCase(
            passage = passage,
            strategy = UpdateReadStatusOfPassageStrategy.Chapter(
                passageIndex = 0,
                chapterIndex = 1,
            ),
        )

        // Then
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `GIVEN a read passage WHEN toggling the entire book THEN the new status is the opposite`() = runTest {
        // Given
        prepareScenario()

        // When
        val result = useCase(
            passage = passage,
            strategy = UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex = 0),
        )

        // Then
        assertEquals(false, result.getOrNull())
    }

    @Test
    fun `GIVEN an out of range chapter index WHEN toggling THEN fails`() = runTest {
        // Given
        prepareScenario()

        // When
        val results = listOf(-1, 2).map { chapterIndex ->
            useCase(
                passage = passage,
                strategy = UpdateReadStatusOfPassageStrategy.Chapter(
                    passageIndex = 0,
                    chapterIndex = chapterIndex,
                ),
            )
        }

        // Then
        assertTrue(results.all(Result<Boolean>::isFailure))
        assertIs<IllegalStateException>(results.first().exceptionOrNull())
    }

    @Test
    fun `GIVEN the book is not in the library WHEN toggling a chapter THEN fails`() = runTest {
        // Given
        prepareScenario(books = emptyList())

        // When
        val result = useCase(
            passage = passage,
            strategy = UpdateReadStatusOfPassageStrategy.Chapter(
                passageIndex = 0,
                chapterIndex = 0,
            ),
        )

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `GIVEN the chapter is not in the book WHEN toggling it THEN fails`() = runTest {
        // Given
        prepareScenario(
            books = listOf(
                BookDataModel(
                    id = BookId.GEN,
                    chapters = emptyList(),
                    isRead = false,
                ),
            ),
        )

        // When
        val result = useCase(
            passage = passage,
            strategy = UpdateReadStatusOfPassageStrategy.Chapter(
                passageIndex = 0,
                chapterIndex = 0,
            ),
        )

        // Then
        assertTrue(result.isFailure)
    }

    private fun prepareScenario(
        books: List<BookDataModel> = listOf(
            BookDataModel(
                id = BookId.GEN,
                chapters = listOf(
                    BookChapterModel(
                        number = 1,
                        verses = listOf(
                            VerseModel(
                                number = 1,
                                isRead = true,
                            ),
                        ),
                        isRead = true,
                        readUpdatedAt = null,
                    ),
                    BookChapterModel(
                        number = 2,
                        verses = listOf(
                            VerseModel(
                                number = 1,
                                isRead = false,
                            ),
                        ),
                        isRead = false,
                        readUpdatedAt = null,
                    ),
                ),
                isRead = false,
            ),
        ),
    ) {
        useCase = IsChapterReadStatusUseCase(GetBooksFlowUseCase(FakeBooksRepository(books)))
    }
}
