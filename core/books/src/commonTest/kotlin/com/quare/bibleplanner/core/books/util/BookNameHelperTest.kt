package com.quare.bibleplanner.core.books.util

import com.quare.bibleplanner.core.model.book.BookId
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BookNameHelperTest {
    @Test
    fun `GIVEN every book WHEN resolving its name resource THEN picks the resource named after the book id`() {
        // When
        val resourceKeys = BookId.entries.map { it.toBookNameResource().key }

        // Then
        assertEquals(BookId.entries.map { "book_${it.name.lowercase()}" }, resourceKeys)
    }

    @Test
    fun `GIVEN every book WHEN resolving its name resource THEN no two books share a name`() {
        // When
        val resources = BookId.entries.map { it.toBookNameResource() }

        // Then
        assertEquals(BookId.entries.size, resources.toSet().size)
    }
}
