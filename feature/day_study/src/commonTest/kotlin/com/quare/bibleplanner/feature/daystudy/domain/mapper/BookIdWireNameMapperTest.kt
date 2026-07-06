package com.quare.bibleplanner.feature.daystudy.domain.mapper

import com.quare.bibleplanner.core.model.book.BookId
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BookIdWireNameMapperTest {
    private val mapper = BookIdWireNameMapper()

    @Test
    fun `GIVEN genesis WHEN mapping THEN returns the full english name`() {
        // Given
        val bookId = BookId.GEN

        // When
        val wireName = mapper.map(bookId)

        // Then
        assertEquals("GENESIS", wireName)
    }

    @Test
    fun `GIVEN a numbered book WHEN mapping THEN spells the ordinal prefix`() {
        // Given
        val bookId = BookId.SECOND_SA

        // When
        val wireName = mapper.map(bookId)

        // Then
        assertEquals("SECOND_SAMUEL", wireName)
    }

    @Test
    fun `GIVEN every book WHEN mapping THEN each wire name is unique`() {
        // Given
        val books = BookId.entries

        // When
        val wireNames = books.map(mapper::map)

        // Then
        assertEquals(books.size, wireNames.toSet().size)
    }
}
