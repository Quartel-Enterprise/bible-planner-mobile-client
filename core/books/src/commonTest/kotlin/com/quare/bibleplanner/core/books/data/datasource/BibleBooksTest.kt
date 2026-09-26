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
    fun `WHEN listing the book files THEN names one json file per book`() {
        // When
        val fileNames = BibleBooks.fileNames

        // Then
        assertEquals(BookId.entries.size, fileNames.size)
        assertTrue(fileNames.all { it.endsWith(".json") })
    }

    @Test
    fun `WHEN resolving each book file THEN maps them to every book in canonical order`() {
        // When
        val bookIds = BibleBooks.fileNames.map { fileNameToBookIdMapper.map(it.removeSuffix(".json")) }

        // Then
        assertEquals(BookId.entries, bookIds)
    }

    @Test
    fun `GIVEN an unknown book code WHEN resolving it THEN returns null`() {
        // When
        val bookId = fileNameToBookIdMapper.map("XYZ")

        // Then
        assertNull(bookId)
    }
}
