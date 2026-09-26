package com.quare.bibleplanner.core.books.data.datasource

import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class BooksLocalDataSourceTest {
    private lateinit var books: List<BookDataModel>

    @BeforeTest
    fun setUp() = runTest {
        books = BooksLocalDataSource(FileNameToBookIdMapper(BookMapsProvider())).getBooks()
    }

    @Test
    fun `WHEN reading the bundled books THEN returns every book in canonical order`() {
        // Then
        assertEquals(BookId.entries, books.map { it.id })
    }

    @Test
    fun `WHEN reading the bundled books THEN numbers each verse of each chapter from the file`() {
        // When
        val genesis = books.first { it.id == BookId.GEN }

        // Then
        assertEquals(50, genesis.chapters.size)
        assertEquals(
            (1..31).toList(),
            genesis.chapters
                .first()
                .verses
                .map { it.number },
        )
    }

    @Test
    fun `WHEN reading the bundled books THEN starts with nothing read`() {
        // When
        val chapters = books.flatMap { it.chapters }

        // Then
        assertFalse(books.any { it.isRead })
        assertFalse(chapters.any { it.isRead })
        assertFalse(chapters.flatMap { it.verses }.any { it.isRead })
    }

    @Test
    fun `WHEN reading the bundled books THEN has the canonical chapter count`() {
        // When
        val chapterCount = books.sumOf { it.chapters.size }

        // Then
        assertEquals(1189, chapterCount)
    }
}
