package com.quare.bibleplanner.core.plan.data.mapper

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChaptersRangeMapperTest {
    private val mapper = ChaptersRangeMapper()

    @Test
    fun `GIVEN no chapters WHEN formatting THEN returns an empty label`() {
        // When
        val label = mapper.map(emptyList())

        // Then
        assertEquals("", label)
    }

    @Test
    fun `GIVEN a single whole chapter WHEN formatting THEN returns its number`() {
        // When
        val label = mapper.map(listOf(chapter(number = 5)))

        // Then
        assertEquals("5", label)
    }

    @Test
    fun `GIVEN consecutive whole chapters out of order WHEN formatting THEN collapses them into one range`() {
        // When
        val label = mapper.map(
            listOf(
                chapter(number = 6),
                chapter(number = 4),
                chapter(number = 5),
            ),
        )

        // Then
        assertEquals("4-6", label)
    }

    @Test
    fun `GIVEN a gap between chapters WHEN formatting THEN lists each run separately`() {
        // When
        val label = mapper.map(
            listOf(
                chapter(number = 32),
                chapter(number = 122),
                chapter(number = 123),
            ),
        )

        // Then
        assertEquals("32, 122-123", label)
    }

    @Test
    fun `GIVEN a chapter with a verse range WHEN formatting THEN appends the verses`() {
        // When
        val label = mapper.map(
            listOf(
                chapter(
                    number = 3,
                    startVerse = 1,
                    endVerse = 10,
                ),
            ),
        )

        // Then
        assertEquals("3:1-10", label)
    }

    @Test
    fun `GIVEN a chapter limited to one verse WHEN formatting THEN shows that verse once`() {
        // When
        val label = mapper.map(
            listOf(
                chapter(
                    number = 3,
                    startVerse = 16,
                    endVerse = 16,
                ),
            ),
        )

        // Then
        assertEquals("3:16", label)
    }

    @Test
    fun `GIVEN only a start or only an end verse WHEN formatting THEN marks the open side`() {
        // When
        val label = mapper.map(
            listOf(
                chapter(
                    number = 1,
                    startVerse = 5,
                ),
                chapter(
                    number = 3,
                    endVerse = 8,
                ),
            ),
        )

        // Then
        assertEquals("1:5, 3:-8", label)
    }

    @Test
    fun `GIVEN consecutive chapters with verse bounds WHEN formatting THEN keeps them as separate entries`() {
        // When
        val label = mapper.map(
            listOf(
                chapter(
                    number = 5,
                    startVerse = 10,
                ),
                chapter(number = 6),
                chapter(
                    number = 7,
                    endVerse = 4,
                ),
            ),
        )

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
