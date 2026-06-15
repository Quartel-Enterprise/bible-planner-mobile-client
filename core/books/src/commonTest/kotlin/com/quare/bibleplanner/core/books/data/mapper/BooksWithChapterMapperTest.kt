package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.room.relation.BookWithChapters
import com.quare.bibleplanner.core.provider.room.relation.ChapterWithVerses
import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BooksWithChapterMapperTest {
    private val mapper = BooksWithChapterMapper()

    @Test
    fun `maps one verse model per verse regardless of how many bible versions are downloaded`() = runTest {
        // A single verse with two downloaded versions (two texts) must still map to one verse, so
        // verse counts don't scale with the number of downloaded versions (which would diverge
        // reading-progress percentages between devices).
        val book = BookWithChapters(
            book = BookEntity(id = "GEN", isRead = false, favoriteUpdatedAt = null, isFavoritePendingSync = false),
            chapters = listOf(
                ChapterWithVerses(
                    chapter = ChapterEntity(id = 1, number = 1, bookId = "GEN", isRead = false),
                    verses = listOf(
                        VerseWithTexts(
                            verse = VerseEntity(id = 1, number = 1, chapterId = 1, isRead = true),
                            texts = listOf(
                                VerseTextEntity(id = 1, verseId = 1, bibleVersionId = "ACF", text = "no princípio"),
                                VerseTextEntity(id = 2, verseId = 1, bibleVersionId = "WEB", text = "in the beginning"),
                            ),
                        ),
                        VerseWithTexts(
                            verse = VerseEntity(id = 2, number = 2, chapterId = 1, isRead = false),
                            texts = emptyList(),
                        ),
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
}
