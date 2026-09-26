package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IsPassageReadUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: IsPassageReadUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        useCase = IsPassageReadUseCase(
            bookDao = database.bookDao,
            chapterDao = database.chapterDao,
            isChapterRead = IsChapterReadUseCase(
                chapterDao = database.chapterDao,
                verseDao = database.verseDao,
                isWholeChapterRead = IsWholeChapterReadUseCase(
                    chapterDao = database.chapterDao,
                    verseDao = database.verseDao,
                ),
            ),
        )
    }

    @Test
    fun `GIVEN a book missing from the database WHEN checking a passage THEN is not read`() = runTest {
        // When
        val result = useCase(passage(bookId = BookId.EXO))

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a book flagged read WHEN checking the whole book THEN is read`() = runTest {
        // Given
        database.seedBook(
            bookId = BookId.OBA,
            versesPerChapter = listOf(21),
            isBookRead = true,
        )

        // When
        val result = useCase(passage(bookId = BookId.OBA))

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a book without chapters WHEN checking the whole book THEN is not read`() = runTest {
        // Given
        database.seedBook(
            bookId = BookId.OBA,
            versesPerChapter = emptyList(),
        )

        // When
        val result = useCase(passage(bookId = BookId.OBA))

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN every chapter read WHEN checking the whole book THEN is read`() = runTest {
        // Given
        database.seedBook(
            bookId = BookId.JUD,
            versesPerChapter = listOf(2),
            readVerses = mapOf(1 to setOf(1, 2)),
        )

        // When
        val result = useCase(passage(bookId = BookId.JUD))

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN an unread chapter WHEN checking the whole book THEN is not read`() = runTest {
        // Given
        database.seedBook(
            bookId = BookId.JUD,
            versesPerChapter = listOf(2, 2),
            readChapters = setOf(1),
        )

        // When
        val result = useCase(passage(bookId = BookId.JUD))

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN only the listed chapters read WHEN checking those chapters THEN is read`() = runTest {
        // Given
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(2, 2, 2),
            readChapters = setOf(1, 2),
        )

        // When
        val result = useCase(
            passage(
                bookId = BookId.GEN,
                chapterNumbers = listOf(1, 2),
            ),
        )

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN one listed chapter unread WHEN checking those chapters THEN is not read`() = runTest {
        // Given
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(2, 2, 2),
            readChapters = setOf(1),
        )

        // When
        val result = useCase(
            passage(
                bookId = BookId.GEN,
                chapterNumbers = listOf(1, 3),
            ),
        )

        // Then
        assertFalse(result)
    }

    private fun passage(
        bookId: BookId,
        chapterNumbers: List<Int> = emptyList(),
    ): PassageModel = PassageModel(
        bookId = bookId,
        chapters = chapterNumbers.map { number ->
            ChapterModel(
                number = number,
                startVerse = null,
                endVerse = null,
                bookId = bookId,
            )
        },
        isRead = false,
        chapterRanges = null,
    )
}
