package com.quare.bibleplanner.core.plan.data.mapper

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChaptersRangeMapperTest {
    private val mapper = ChaptersRangeMapper()

    @Test
    fun `GIVEN no chapters WHEN formatting THEN returns an empty label`() {
        // Given
        val chapters = emptyList<ChapterModel>()

        // When
        val label = mapper.map(chapters)

        // Then
        assertEquals("", label)
    }

    @Test
    fun `GIVEN a single whole chapter WHEN formatting THEN returns its number`() {
        // Given
        val chapters = listOf(chapter(number = 5))

        // When
        val label = mapper.map(chapters)

        // Then
        assertEquals("5", label)
    }

    @Test
    fun `GIVEN consecutive whole chapters out of order WHEN formatting THEN collapses them into one range`() {
        // Given
        val chapters = listOf(
            chapter(number = 6),
            chapter(number = 4),
            chapter(number = 5),
        )

        // When
        val label = mapper.map(chapters)

        // Then
        assertEquals("4-6", label)
    }

    @Test
    fun `GIVEN a gap between chapters WHEN formatting THEN lists each run separately`() {
        // Given
        val chapters = listOf(
            chapter(number = 32),
            chapter(number = 122),
            chapter(number = 123),
        )

        // When
        val label = mapper.map(chapters)

        // Then
        assertEquals("32, 122-123", label)
    }

    @Test
    fun `GIVEN a chapter with a verse range WHEN formatting THEN appends the verses`() {
        // Given
        val chapters = listOf(
            chapter(
                number = 3,
                startVerse = 1,
                endVerse = 10,
            ),
        )

        // When
        val label = mapper.map(chapters)

        // Then
        assertEquals("3:1-10", label)
    }

    @Test
    fun `GIVEN a chapter limited to one verse WHEN formatting THEN shows that verse once`() {
        // Given
        val chapters = listOf(
            chapter(
                number = 3,
                startVerse = 16,
                endVerse = 16,
            ),
        )

        // When
        val label = mapper.map(chapters)

        // Then
        assertEquals("3:16", label)
    }

    @Test
    fun `GIVEN only a start or only an end verse WHEN formatting THEN marks the open side`() {
        // Given
        val chapters = listOf(
            chapter(
                number = 1,
                startVerse = 5,
            ),
            chapter(
                number = 3,
                endVerse = 8,
            ),
        )

        // When
        val label = mapper.map(chapters)

        // Then
        assertEquals("1:5, 3:-8", label)
    }

    @Test
    fun `GIVEN consecutive chapters with verse bounds WHEN formatting THEN keeps them as separate entries`() {
        // Given
        val chapters = listOf(
            chapter(
                number = 5,
                startVerse = 10,
            ),
            chapter(number = 6),
            chapter(
                number = 7,
                endVerse = 4,
            ),
        )

        // When
        val label = mapper.map(chapters)

        // Then
        assertEquals("5:10, 6, 7:-4", label)
    }

    private fun chapter(
        number: Int,
        startVerse: Int? = null,
        endVerse: Int? = null,
    ): ChapterModel = ChapterModel(
        number = number,
        startVerse = startVerse,
        endVerse = endVerse,
        bookId = BookId.PSA,
    )
}
