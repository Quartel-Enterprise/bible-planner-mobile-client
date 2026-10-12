package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.ChapterVerseContentModel
import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetChapterVerseContentsUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: GetChapterVerseContentsUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(3),
        )
        useCase = GetChapterVerseContentsUseCase(
            getChapterId = GetChapterIdUseCase(database.chapterDao),
            verseDao = database.verseDao,
        )
    }

    @Test
    fun `GIVEN texts with headings WHEN getting a chapter THEN trims the texts and keeps the headings as they are`() =
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
                text = " In the beginning ",
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
            val contents = useCase(
                ChapterRef(
                    bibleVersionId = "web",
                    bookId = BookId.GEN,
                    chapterNumber = 1,
                ),
            )

            // Then
            assertEquals(
                mapOf(
                    1 to ChapterVerseContentModel(
                        text = "In the beginning",
                        heading = " The Creation",
                    ),
                    2 to ChapterVerseContentModel(
                        text = "The earth was formless",
                        heading = null,
                    ),
                    3 to ChapterVerseContentModel(
                        text = "God said",
                        heading = null,
                    ),
                ),
                contents,
            )
        }
}
