package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.BookId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class WireNameBookIdMapperTest {
    private lateinit var bookIdWireNameMapper: BookIdWireNameMapper
    private lateinit var mapper: WireNameBookIdMapper

    @BeforeTest
    fun setUp() {
        bookIdWireNameMapper = BookIdWireNameMapper()
        mapper = WireNameBookIdMapper(bookIdWireNameMapper)
    }

    @Test
    fun `GIVEN the wire name of every book WHEN mapping it back THEN returns the same book`() {
        // Given
        val books = BookId.entries.toList()
        val wireNames = books.map(bookIdWireNameMapper::map)

        // When
        val mappedBooks = wireNames.map(mapper::mapOrNull)

        // Then
        assertEquals(
            expected = books,
            actual = mappedBooks,
        )
    }

    @Test
    fun `GIVEN a name that is not a wire name WHEN mapping THEN returns null`() {
        // When & Then
        assertNull(mapper.mapOrNull("TOBIT"))
        assertNull(mapper.mapOrNull("GEN"))
        assertNull(mapper.mapOrNull("genesis"))
        assertNull(mapper.mapOrNull(""))
    }
}
