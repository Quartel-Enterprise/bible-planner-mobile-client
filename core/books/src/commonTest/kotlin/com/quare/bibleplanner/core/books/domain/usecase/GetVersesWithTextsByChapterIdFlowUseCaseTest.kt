package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetVersesWithTextsByChapterIdFlowUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: GetVersesWithTextsByChapterIdFlowUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(2, 1),
        )
        useCase = GetVersesWithTextsByChapterIdFlowUseCase(database.verseDao)
    }

    @Test
    fun `GIVEN verse texts WHEN observing a chapter THEN emits only that chapter verses with their texts`() = runTest {
        // Given
        val chapterId = database
            .chapter(
                bookId = BookId.GEN,
                chapterNumber = 1,
            ).id
        val firstVerse = database.verses.first { it.chapterId == chapterId }
        database.verseTexts += VerseTextEntity(
            verseId = firstVerse.id,
            bibleVersionId = "WEB",
            text = "In the beginning",
        )

        // When
        val verses = useCase(chapterId).first()

        // Then
        assertEquals(listOf(1, 2), verses.map { it.verse.number })
        assertEquals(listOf("In the beginning"), verses.first().texts.map { it.text })
    }
}
