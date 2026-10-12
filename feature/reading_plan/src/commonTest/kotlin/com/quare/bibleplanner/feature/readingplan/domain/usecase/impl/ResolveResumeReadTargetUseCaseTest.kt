package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.testing.FakeBooksRepository
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.readingplan.domain.model.ResumeReadTarget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ResolveResumeReadTargetUseCaseTest {
    private lateinit var useCase: ResolveResumeReadTargetUseCase

    @Test
    fun `GIVEN no chapter of the day read WHEN resolving THEN targets the first chapter of the first passage`() =
        runTest {
            // Given
            val passages = listOf(
                passageOf(
                    bookId = BookId.JOB,
                    chapterNumbers = listOf(32, 33, 34),
                ),
            )
            prepareScenario(
                books = listOf(
                    bookOf(
                        id = BookId.JOB,
                        chapters = listOf(
                            bookChapter(number = 32),
                            bookChapter(number = 33),
                            bookChapter(number = 34),
                        ),
                    ),
                ),
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.JOB,
                        chapterNumber = 32,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN the first chapter of the day read WHEN resolving THEN targets the next unread chapter`() = runTest {
        // Given
        val passages = listOf(
            passageOf(
                bookId = BookId.JOB,
                chapterNumbers = listOf(32, 33, 34),
            ),
        )
        prepareScenario(
            books = listOf(
                bookOf(
                    id = BookId.JOB,
                    chapters = listOf(
                        bookChapter(number = 32, isRead = true),
                        bookChapter(number = 33),
                        bookChapter(number = 34),
                    ),
                ),
            ),
        )

        // When
        val result = useCase(passages)

        // Then
        assertEquals(
            expected = Result.success<ResumeReadTarget?>(
                ResumeReadTarget(
                    bookId = BookId.JOB,
                    chapterNumber = 33,
                    isChapterRead = false,
                ),
            ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN the first passage fully read WHEN resolving THEN targets the first chapter of the second passage`() =
        runTest {
            // Given
            val passages = listOf(
                passage(bookId = BookId.GEN),
                passage(bookId = BookId.EXO),
            )
            prepareScenario(
                books = listOf(
                    bookOf(
                        id = BookId.GEN,
                        chapters = listOf(bookChapter(number = 1, isRead = true)),
                    ),
                    bookOf(
                        id = BookId.EXO,
                        chapters = listOf(bookChapter(number = 1)),
                    ),
                ),
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.EXO,
                        chapterNumber = 1,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN a chapter range whose verses are all read WHEN resolving THEN skips it for the next chapter`() =
        runTest {
            // Given
            val passages = listOf(
                PassageModel(
                    bookId = BookId.PSA,
                    chapters = listOf(
                        ChapterModel(
                            number = 1,
                            startVerse = 2,
                            endVerse = 3,
                            bookId = BookId.PSA,
                        ),
                        ChapterModel(
                            number = 2,
                            startVerse = null,
                            endVerse = null,
                            bookId = BookId.PSA,
                        ),
                    ),
                    isRead = false,
                    chapterRanges = null,
                ),
            )
            prepareScenario(
                books = listOf(
                    bookOf(
                        id = BookId.PSA,
                        chapters = listOf(
                            bookChapter(
                                number = 1,
                                isRead = false,
                                verseCount = 4,
                                readVerses = setOf(2, 3),
                            ),
                            bookChapter(number = 2),
                        ),
                    ),
                ),
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.PSA,
                        chapterNumber = 2,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN a chapter range with one unread verse WHEN resolving THEN targets that chapter`() = runTest {
        // Given
        val passages = listOf(
            PassageModel(
                bookId = BookId.PSA,
                chapters = listOf(
                    ChapterModel(
                        number = 1,
                        startVerse = 2,
                        endVerse = 3,
                        bookId = BookId.PSA,
                    ),
                    ChapterModel(
                        number = 2,
                        startVerse = null,
                        endVerse = null,
                        bookId = BookId.PSA,
                    ),
                ),
                isRead = false,
                chapterRanges = null,
            ),
        )
        prepareScenario(
            books = listOf(
                bookOf(
                    id = BookId.PSA,
                    chapters = listOf(
                        bookChapter(
                            number = 1,
                            isRead = false,
                            verseCount = 4,
                            readVerses = setOf(1, 2, 4),
                        ),
                        bookChapter(number = 2),
                    ),
                ),
            ),
        )

        // When
        val result = useCase(passages)

        // Then
        assertEquals(
            expected = Result.success<ResumeReadTarget?>(
                ResumeReadTarget(
                    bookId = BookId.PSA,
                    chapterNumber = 1,
                    isChapterRead = false,
                ),
            ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN a ranged chapter flagged read WHEN resolving THEN treats it as read and targets the next chapter`() =
        runTest {
            // Given
            val passages = listOf(
                PassageModel(
                    bookId = BookId.PSA,
                    chapters = listOf(
                        ChapterModel(
                            number = 1,
                            startVerse = 2,
                            endVerse = 3,
                            bookId = BookId.PSA,
                        ),
                        ChapterModel(
                            number = 2,
                            startVerse = null,
                            endVerse = null,
                            bookId = BookId.PSA,
                        ),
                    ),
                    isRead = false,
                    chapterRanges = null,
                ),
            )
            prepareScenario(
                books = listOf(
                    bookOf(
                        id = BookId.PSA,
                        chapters = listOf(
                            bookChapter(
                                number = 1,
                                isRead = true,
                                verseCount = 4,
                                readVerses = emptySet(),
                            ),
                            bookChapter(number = 2),
                        ),
                    ),
                ),
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.PSA,
                        chapterNumber = 2,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN a whole book passage with early chapters read WHEN resolving THEN targets the first unread chapter`() =
        runTest {
            // Given
            val passages = listOf(
                passageOf(
                    bookId = BookId.JOB,
                    chapterNumbers = emptyList(),
                ),
            )
            prepareScenario(
                books = listOf(
                    bookOf(
                        id = BookId.JOB,
                        chapters = listOf(
                            bookChapter(number = 1, isRead = true),
                            bookChapter(number = 2, isRead = true),
                            bookChapter(number = 3),
                            bookChapter(number = 4),
                        ),
                    ),
                ),
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.JOB,
                        chapterNumber = 3,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN a whole book passage with every chapter read WHEN resolving THEN moves on to the next passage`() =
        runTest {
            // Given
            val passages = listOf(
                passageOf(
                    bookId = BookId.JOB,
                    chapterNumbers = emptyList(),
                ),
                passage(bookId = BookId.EXO),
            )
            prepareScenario(
                books = listOf(
                    bookOf(
                        id = BookId.JOB,
                        chapters = listOf(
                            bookChapter(number = 1, isRead = true),
                            bookChapter(number = 2, isRead = true),
                        ),
                    ),
                    bookOf(
                        id = BookId.EXO,
                        chapters = listOf(bookChapter(number = 1)),
                    ),
                ),
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.EXO,
                        chapterNumber = 1,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN a whole book passage of a book missing from the repository WHEN resolving THEN targets chapter 1`() =
        runTest {
            // Given
            val passages = listOf(
                passageOf(
                    bookId = BookId.JOB,
                    chapterNumbers = emptyList(),
                    isRead = false,
                ),
            )
            prepareScenario(books = emptyList())

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.JOB,
                        chapterNumber = 1,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN a read whole book passage of a missing book WHEN resolving THEN moves on to the next passage`() =
        runTest {
            // Given
            val passages = listOf(
                passageOf(
                    bookId = BookId.JOB,
                    chapterNumbers = emptyList(),
                    isRead = true,
                ),
                passage(bookId = BookId.EXO),
            )
            prepareScenario(books = emptyList())

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.EXO,
                        chapterNumber = 1,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN a chapter of the day missing from the repository book WHEN resolving THEN counts it as unread`() =
        runTest {
            // Given
            val passages = listOf(
                passageOf(
                    bookId = BookId.JOB,
                    chapterNumbers = listOf(1, 2),
                ),
            )
            prepareScenario(
                books = listOf(
                    bookOf(
                        id = BookId.JOB,
                        chapters = listOf(bookChapter(number = 1, isRead = true)),
                    ),
                ),
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.JOB,
                        chapterNumber = 2,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN chapters of a book missing from the repository WHEN resolving THEN targets the first listed chapter`() =
        runTest {
            // Given
            val passages = listOf(
                passageOf(
                    bookId = BookId.JOB,
                    chapterNumbers = listOf(32, 33),
                ),
            )
            prepareScenario(books = emptyList())

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.JOB,
                        chapterNumber = 32,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN every chapter of the day read WHEN resolving THEN falls back to the first chapter flagged read`() =
        runTest {
            // Given
            val passages = listOf(
                passageOf(
                    bookId = BookId.JOB,
                    chapterNumbers = listOf(32, 33),
                    isRead = true,
                ),
                passage(bookId = BookId.EXO, isRead = true),
            )
            prepareScenario(
                books = listOf(
                    bookOf(
                        id = BookId.JOB,
                        chapters = listOf(
                            bookChapter(number = 32, isRead = true),
                            bookChapter(number = 33, isRead = true),
                        ),
                    ),
                    bookOf(
                        id = BookId.EXO,
                        chapters = listOf(bookChapter(number = 1, isRead = true)),
                    ),
                ),
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.JOB,
                        chapterNumber = 32,
                        isChapterRead = true,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN only a read verse range of the day WHEN resolving THEN falls back to its chapter flagged unread`() =
        runTest {
            // Given
            val passages = listOf(
                PassageModel(
                    bookId = BookId.PSA,
                    chapters = listOf(
                        ChapterModel(
                            number = 119,
                            startVerse = 1,
                            endVerse = 2,
                            bookId = BookId.PSA,
                        ),
                    ),
                    isRead = true,
                    chapterRanges = null,
                ),
            )
            prepareScenario(
                books = listOf(
                    bookOf(
                        id = BookId.PSA,
                        chapters = listOf(
                            bookChapter(
                                number = 119,
                                isRead = false,
                                verseCount = 4,
                                readVerses = setOf(1, 2),
                            ),
                        ),
                    ),
                ),
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.PSA,
                        chapterNumber = 119,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN a whole book passage fully read WHEN resolving THEN falls back to chapter 1 flagged read`() = runTest {
        // Given
        val passages = listOf(
            passageOf(
                bookId = BookId.JOB,
                chapterNumbers = emptyList(),
                isRead = true,
            ),
        )
        prepareScenario(
            books = listOf(
                bookOf(
                    id = BookId.JOB,
                    chapters = listOf(
                        bookChapter(number = 1, isRead = true),
                        bookChapter(number = 2, isRead = true),
                    ),
                ),
            ),
        )

        // When
        val result = useCase(passages)

        // Then
        assertEquals(
            expected = Result.success<ResumeReadTarget?>(
                ResumeReadTarget(
                    bookId = BookId.JOB,
                    chapterNumber = 1,
                    isChapterRead = true,
                ),
            ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN no passages WHEN resolving THEN returns a null target`() = runTest {
        // Given
        prepareScenario(books = emptyList())

        // When
        val result = useCase(emptyList())

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `GIVEN a repository that cannot list every book WHEN resolving THEN resolves from the books of the day`() =
        runTest {
            // Given
            val passages = listOf(
                passageOf(
                    bookId = BookId.JOB,
                    chapterNumbers = listOf(32, 33),
                ),
            )
            val dayBook = bookOf(
                id = BookId.JOB,
                chapters = listOf(
                    bookChapter(number = 32, isRead = true),
                    bookChapter(number = 33),
                ),
            )
            prepareScenario(
                booksRepository = object : BooksRepository by FakeBooksRepository(books = listOf(dayBook)) {
                    override suspend fun getBooks(): List<BookDataModel> =
                        throw IllegalStateException("the whole Bible must not be loaded")
                },
            )

            // When
            val result = useCase(passages)

            // Then
            assertEquals(
                expected = Result.success<ResumeReadTarget?>(
                    ResumeReadTarget(
                        bookId = BookId.JOB,
                        chapterNumber = 33,
                        isChapterRead = false,
                    ),
                ),
                actual = result,
            )
        }

    @Test
    fun `GIVEN a repository that fails WHEN resolving THEN returns the failure`() = runTest {
        // Given
        val passages = listOf(passage(bookId = BookId.GEN))
        prepareScenario(
            booksRepository = object : BooksRepository by FakeBooksRepository(books = emptyList()) {
                override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> =
                    flow { throw IllegalStateException("database closed") }
            },
        )

        // When
        val result = useCase(passages)

        // Then
        val error = result.exceptionOrNull()
        assertIs<IllegalStateException>(error)
        assertEquals("database closed", error.message)
    }

    @Test
    fun `GIVEN a cancelled repository read WHEN resolving THEN propagates the cancellation`() = runTest {
        // Given
        val passages = listOf(passage(bookId = BookId.GEN))
        prepareScenario(
            booksRepository = object : BooksRepository by FakeBooksRepository(books = emptyList()) {
                override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> =
                    flow { throw CancellationException("cancelled") }
            },
        )

        // When
        val thrown = assertFailsWith<CancellationException> { useCase(passages) }

        // Then
        assertEquals("cancelled", thrown.message)
    }

    private fun prepareScenario(books: List<BookDataModel>) {
        prepareScenario(booksRepository = FakeBooksRepository(books = books))
    }

    private fun prepareScenario(booksRepository: BooksRepository) {
        useCase = ResolveResumeReadTargetUseCase(booksRepository = booksRepository)
    }
}

private fun passageOf(
    bookId: BookId,
    chapterNumbers: List<Int>,
    isRead: Boolean = false,
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
    isRead = isRead,
    chapterRanges = null,
)

private fun bookOf(
    id: BookId,
    chapters: List<BookChapterModel>,
): BookDataModel = BookDataModel(
    id = id,
    chapters = chapters,
    isRead = false,
    isFavorite = false,
)

private fun bookChapter(
    number: Int,
    isRead: Boolean = false,
    verseCount: Int = 0,
    readVerses: Set<Int> = emptySet(),
): BookChapterModel = BookChapterModel(
    number = number,
    verses = (1..verseCount).map { verseNumber ->
        VerseModel(
            number = verseNumber,
            isRead = verseNumber in readVerses,
        )
    },
    isRead = isRead,
    readUpdatedAt = null,
)
