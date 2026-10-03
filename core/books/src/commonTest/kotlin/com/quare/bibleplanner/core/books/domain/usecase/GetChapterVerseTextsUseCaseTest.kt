package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetChapterVerseTextsUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: GetChapterVerseTextsUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(3),
        )
        useCase = GetChapterVerseTextsUseCase(
            getChapterId = GetChapterIdUseCase(database.chapterDao),
            verseDao = database.verseDao,
        )
    }

    @Test
    fun `GIVEN texts in two versions WHEN getting a chapter THEN returns the trimmed texts of its version only`() =
        runTest {
            // Given
            val chapterId = database
                .chapter(
                    bookId = BookId.GEN,
                    chapterNumber = 1,
                ).id
            val (first, second) = database.verses.filter { it.chapterId == chapterId }
            database.verseTexts += VerseTextEntity(
                verseId = first.id,
                bibleVersionId = "web",
                text = " In the beginning ",
                id = 0,
                heading = null,
            )
            database.verseTexts += VerseTextEntity(
                verseId = second.id,
                bibleVersionId = "web",
                text = "The earth was formless",
                id = 0,
                heading = null,
            )
            database.verseTexts += VerseTextEntity(
                verseId = first.id,
                bibleVersionId = "arc",
                text = "No princípio",
                id = 0,
                heading = null,
            )

            // When
            val texts = useCase(
                ChapterRef(
                    bibleVersionId = "web",
                    bookId = BookId.GEN,
                    chapterNumber = 1,
                ),
            )

            // Then
            assertEquals(
                mapOf(
                    1 to "In the beginning",
                    2 to "The earth was formless",
                ),
                texts,
            )
        }

    @Test
    fun `GIVEN an unknown chapter WHEN getting its texts THEN returns an empty map`() = runTest {
        // When
        val texts = useCase(
            ChapterRef(
                bibleVersionId = "web",
                bookId = BookId.EXO,
                chapterNumber = 1,
            ),
        )

        // Then
        assertEquals(emptyMap(), texts)
    }
}
