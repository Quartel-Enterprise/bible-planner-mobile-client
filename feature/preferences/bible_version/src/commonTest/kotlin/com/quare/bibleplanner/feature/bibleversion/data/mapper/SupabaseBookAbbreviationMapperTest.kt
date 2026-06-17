package com.quare.bibleplanner.feature.bibleversion.data.mapper

import com.quare.bibleplanner.core.model.book.BookId
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SupabaseBookAbbreviationMapperTest {
    private val mapper = SupabaseBookAbbreviationMapper()

    @Test
    fun `GIVEN the gospel of John WHEN mapping THEN returns its own directory not Job's`() {
        // Given
        val bookId = BookId.JHN

        // When
        val abbreviation = mapper.map(bookId)

        // Then
        assertEquals("Jh", abbreviation)
    }

    @Test
    fun `GIVEN the book of Job WHEN mapping THEN returns Jo`() {
        // Given
        val bookId = BookId.JOB

        // When
        val abbreviation = mapper.map(bookId)

        // Then
        assertEquals("Jo", abbreviation)
    }

    @Test
    fun `GIVEN every book WHEN mapping THEN each abbreviation is unique`() {
        // Given
        val books = BookId.entries

        // When
        val abbreviations = books.map(mapper::map)

        // Then
        assertEquals(books.size, abbreviations.toSet().size)
    }
}
