package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.relation.BookWithChapters
import com.quare.bibleplanner.core.provider.room.relation.ChapterWithVerses
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BooksWithChapterMapperTest {
    private val mapper = BooksWithChapterMapper()

    @Test
    fun `maps one verse model per verse`() = runTest {
        val book = BookWithChapters(
            book = BookEntity(id = "GEN", isRead = false, favoriteUpdatedAt = null, isFavoritePendingSync = false),
            chapters = listOf(
                ChapterWithVerses(
                    chapter = ChapterEntity(id = 1, number = 1, bookId = "GEN", isRead = false),
                    verses = listOf(
                        VerseEntity(id = 1, number = 1, chapterId = 1, isRead = true),
                        VerseEntity(id = 2, number = 2, chapterId = 1, isRead = false),
                    ),
                ),
            ),
        )

        val result = mapper.mapModel(book)

        val verses = result.chapters.single().verses
        assertEquals(2, verses.size)
        assertEquals(listOf(1, 2), verses.map { it.number })
        assertEquals(1, verses.count { it.isRead })
    }

    @Test
    fun `chapter read date is the latest read moment across the chapter flag and its verses`() = runTest {
        // Given
        val book = BookWithChapters(
            book = BookEntity(id = "GEN", isRead = false, favoriteUpdatedAt = null, isFavoritePendingSync = false),
            chapters = listOf(
                ChapterWithVerses(
                    chapter = ChapterEntity(id = 1, number = 1, bookId = "GEN", isRead = true, readUpdatedAt = 100L),
                    verses = listOf(
                        VerseEntity(id = 1, number = 1, chapterId = 1, isRead = true, readUpdatedAt = 250L),
                    ),
                ),
            ),
        )

        // When
        val result = mapper.mapModel(book)

        // Then
        assertEquals(250L, result.chapters.single().readUpdatedAt)
    }

    @Test
    fun `chapter read date is null when neither the chapter nor its verses were read`() = runTest {
        // Given
        val book = BookWithChapters(
            book = BookEntity(id = "GEN", isRead = false, favoriteUpdatedAt = null, isFavoritePendingSync = false),
            chapters = listOf(
                ChapterWithVerses(
                    chapter = ChapterEntity(id = 1, number = 1, bookId = "GEN", isRead = false),
                    verses = listOf(
                        VerseEntity(id = 1, number = 1, chapterId = 1, isRead = false),
                    ),
                ),
            ),
        )

        // When
        val result = mapper.mapModel(book)

        // Then
        assertEquals(null, result.chapters.single().readUpdatedAt)
    }
}
