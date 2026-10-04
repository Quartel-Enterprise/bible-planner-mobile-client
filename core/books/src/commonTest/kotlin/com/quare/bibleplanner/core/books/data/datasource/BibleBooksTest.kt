package com.quare.bibleplanner.core.books.data.datasource

import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.model.book.BookId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BibleBooksTest {
    private val fileNameToBookIdMapper = FileNameToBookIdMapper(BookMapsProvider())

    @Test
    fun `GIVEN every book WHEN listing the book files THEN names one json file per book`() {
        // Given
        val bookCount = BookId.entries.size

        // When
        val fileNames = BibleBooks.fileNames

        // Then
        assertEquals(bookCount, fileNames.size)
        assertTrue(fileNames.all { it.endsWith(".json") })
    }

    @Test
    fun `GIVEN the book files WHEN resolving each book file THEN maps them to every book in canonical order`() {
        // Given
        val fileNames = BibleBooks.fileNames

        // When
        val bookIds = fileNames.map { fileNameToBookIdMapper.map(it.removeSuffix(".json")) }

        // Then
        assertEquals(BookId.entries, bookIds)
    }

    @Test
    fun `GIVEN an unknown book code WHEN resolving it THEN returns null`() {
        // Given
        val unknownBookCode = "XYZ"

        // When
        val bookId = fileNameToBookIdMapper.map(unknownBookCode)

        // Then
        assertNull(bookId)
    }
}
