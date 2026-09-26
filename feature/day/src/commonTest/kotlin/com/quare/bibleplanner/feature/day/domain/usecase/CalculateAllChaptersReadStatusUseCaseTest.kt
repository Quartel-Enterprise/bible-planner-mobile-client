package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CalculateAllChaptersReadStatusUseCaseTest {
    private lateinit var useCase: CalculateAllChaptersReadStatusUseCase

    @BeforeTest
    fun setUp() {
        useCase = CalculateAllChaptersReadStatusUseCase()
    }

    @Test
    fun `GIVEN read and unread chapters WHEN calculating THEN maps each passage and chapter index to its status`() {
        // Given
        val passages = listOf(
            passage(
                bookId = BookId.GEN,
                chapters = listOf(chapter(BookId.GEN, 1), chapter(BookId.GEN, 2)),
            ),
            passage(
                bookId = BookId.EXO,
                chapters = listOf(chapter(BookId.EXO, 1)),
            ),
        )
        val books = listOf(
            book(
                id = BookId.GEN,
                chapters = listOf(
                    bookChapter(
                        number = 1,
                        isRead = true,
                    ),
                    bookChapter(
                        number = 2,
                        isRead = false,
                    ),
                ),
            ),
            book(
                id = BookId.EXO,
                chapters = listOf(
                    bookChapter(
                        number = 1,
                        isRead = true,
                    ),
                ),
            ),
        )

        // When
        val status = useCase(
            passages = passages,
            books = books,
        )

        // Then
        assertEquals(
            mapOf(
                (0 to 0) to true,
                (0 to 1) to false,
                (1 to 0) to true,
            ),
            status,
        )
    }

    @Test
    fun `GIVEN a partially read verse range WHEN calculating THEN only the fully read range counts`() {
        // Given
        val passages = listOf(
            passage(
                bookId = BookId.GEN,
                chapters = listOf(
                    ChapterModel(
                        number = 1,
                        startVerse = 1,
                        endVerse = 2,
                        bookId = BookId.GEN,
                    ),
                    ChapterModel(
                        number = 1,
                        startVerse = 2,
                        endVerse = 3,
                        bookId = BookId.GEN,
                    ),
                ),
            ),
        )
        val books = listOf(
            book(
                id = BookId.GEN,
                chapters = listOf(
                    BookChapterModel(
                        number = 1,
                        verses = listOf(
                            VerseModel(
                                number = 1,
                                isRead = true,
                            ),
                            VerseModel(
                                number = 2,
                                isRead = true,
                            ),
                            VerseModel(
                                number = 3,
                                isRead = false,
                            ),
                        ),
                        isRead = false,
                        readUpdatedAt = null,
                    ),
                ),
            ),
        )

        // When
        val status = useCase(
            passages = passages,
            books = books,
        )

        // Then
        assertEquals(mapOf((0 to 0) to true, (0 to 1) to false), status)
    }

    @Test
    fun `GIVEN a book or chapter missing from the library WHEN calculating THEN treats it as unread`() {
        // Given
        val passages = listOf(
            passage(
                bookId = BookId.GEN,
                chapters = listOf(chapter(BookId.GEN, 5)),
            ),
            passage(
                bookId = BookId.REV,
                chapters = listOf(chapter(BookId.REV, 1)),
            ),
        )
        val books = listOf(
            book(
                id = BookId.GEN,
                chapters = listOf(
                    bookChapter(
                        number = 1,
                        isRead = true,
                    ),
                ),
            ),
        )

        // When
        val status = useCase(
            passages = passages,
            books = books,
        )

        // Then
        assertEquals(mapOf((0 to 0) to false, (1 to 0) to false), status)
    }

    private fun passage(
        bookId: BookId,
        chapters: List<ChapterModel>,
    ): PassageModel = PassageModel(
        bookId = bookId,
        chapters = chapters,
        isRead = false,
        chapterRanges = null,
    )

    private fun chapter(
        bookId: BookId,
        number: Int,
    ): ChapterModel = ChapterModel(
        number = number,
        startVerse = null,
        endVerse = null,
        bookId = bookId,
    )

    private fun book(
        id: BookId,
        chapters: List<BookChapterModel>,
    ): BookDataModel = BookDataModel(
        id = id,
        chapters = chapters,
        isRead = false,
    )

    private fun bookChapter(
        number: Int,
        isRead: Boolean,
    ): BookChapterModel = BookChapterModel(
        number = number,
        verses = listOf(
            VerseModel(
                number = 1,
                isRead = isRead,
            ),
        ),
        isRead = isRead,
        readUpdatedAt = null,
    )
}
