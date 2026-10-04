package com.quare.bibleplanner.core.books.util

import com.quare.bibleplanner.core.model.book.BookId
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BookNameHelperTest {
    @Test
    fun `GIVEN every book WHEN resolving its name resource THEN picks the resource named after the book id`() {
        // Given
        val books = BookId.entries

        // When
        val resourceKeys = books.map { it.toBookNameResource().key }

        // Then
        assertEquals(books.map { "book_${it.name.lowercase()}" }, resourceKeys)
    }

    @Test
    fun `GIVEN every book WHEN resolving its name resource THEN no two books share a name`() {
        // Given
        val books = BookId.entries

        // When
        val resources = books.map { it.toBookNameResource() }

        // Then
        assertEquals(books.size, resources.toSet().size)
    }
}
