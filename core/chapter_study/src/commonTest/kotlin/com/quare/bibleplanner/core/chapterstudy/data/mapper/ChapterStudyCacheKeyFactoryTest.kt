package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChapterStudyCacheKeyFactoryTest {
    private val chapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private lateinit var factory: ChapterStudyCacheKeyFactory

    @BeforeTest
    fun setUp() {
        factory = ChapterStudyCacheKeyFactory()
    }

    @Test
    fun `GIVEN a chapter and a language WHEN creating THEN joins book chapter version and language with pipes`() {
        // When
        val cacheKey = factory.create(
            chapter = chapter,
            languageCode = "pt-BR",
        )

        // Then
        assertEquals(
            expected = "GEN|3|ACF|pt-BR",
            actual = cacheKey,
        )
    }

    @Test
    fun `GIVEN another book chapter version or language WHEN creating THEN every key is different`() {
        // Given
        val chapters = listOf(
            chapter,
            chapter.copy(bookId = BookId.EXO),
            chapter.copy(chapterNumber = 4),
            chapter.copy(bibleVersionId = "NVI"),
        )

        // When
        val cacheKeys = chapters.map { variant ->
            factory.create(
                chapter = variant,
                languageCode = "pt-BR",
            )
        } + factory.create(
            chapter = chapter,
            languageCode = "en",
        )

        // Then
        assertEquals(
            expected = listOf(
                "GEN|3|ACF|pt-BR",
                "EXO|3|ACF|pt-BR",
                "GEN|4|ACF|pt-BR",
                "GEN|3|NVI|pt-BR",
                "GEN|3|ACF|en",
            ),
            actual = cacheKeys,
        )
    }
}
