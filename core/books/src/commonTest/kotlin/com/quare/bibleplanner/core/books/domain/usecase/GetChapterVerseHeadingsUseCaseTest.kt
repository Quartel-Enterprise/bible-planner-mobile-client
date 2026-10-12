package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetChapterVerseHeadingsUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: GetChapterVerseHeadingsUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(3),
        )
        useCase = GetChapterVerseHeadingsUseCase(
            getChapterId = GetChapterIdUseCase(database.chapterDao),
            verseDao = database.verseDao,
        )
    }

    @Test
    fun `GIVEN headings in two versions WHEN getting a chapter THEN returns the untrimmed headings of its version`() =
        runTest {
            // Given
            val chapterId = database
                .chapter(
                    bookId = BookId.GEN,
                    chapterNumber = 1,
                ).id
            val (first, second, third) = database.verses.filter { it.chapterId == chapterId }
            database.verseTexts += VerseTextEntity(
                verseId = first.id,
                bibleVersionId = "web",
                text = "In the beginning",
                id = 0,
                heading = " The Creation",
            )
            database.verseTexts += VerseTextEntity(
                verseId = second.id,
                bibleVersionId = "web",
                text = "The earth was formless",
                id = 0,
                heading = null,
            )
            database.verseTexts += VerseTextEntity(
                verseId = third.id,
                bibleVersionId = "web",
                text = "God said",
                id = 0,
                heading = " ",
            )
            database.verseTexts += VerseTextEntity(
                verseId = first.id,
                bibleVersionId = "arc",
                text = "No princípio",
                id = 0,
                heading = "A criação",
            )

            // When
            val headings = useCase(
                ChapterRef(
                    bibleVersionId = "web",
                    bookId = BookId.GEN,
                    chapterNumber = 1,
                ),
            )

            // Then
            assertEquals(mapOf(1 to " The Creation"), headings)
        }

    @Test
    fun `GIVEN an unknown chapter WHEN getting its headings THEN returns none`() = runTest {
        // When
        val headings = useCase(
            ChapterRef(
                bibleVersionId = "web",
                bookId = BookId.EXO,
                chapterNumber = 1,
            ),
        )

        // Then
        assertEquals(emptyMap(), headings)
    }
}
