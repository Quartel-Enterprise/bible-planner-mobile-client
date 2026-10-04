package com.quare.bibleplanner.core.books.data.datasource

import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class BooksLocalDataSourceTest {
    private lateinit var dataSource: BooksLocalDataSource

    @BeforeTest
    fun setUp() {
        dataSource = BooksLocalDataSource(FileNameToBookIdMapper(BookMapsProvider()))
    }

    @Test
    fun `GIVEN the bundled book files WHEN reading the books THEN returns every book in canonical order`() = runTest {
        // When
        val books = dataSource.getBooks()

        // Then
        assertEquals(BookId.entries, books.map { it.id })
    }

    @Test
    fun `GIVEN the bundled book files WHEN reading the books THEN numbers each verse of each chapter from the file`() =
        runTest {
            // When
            val books = dataSource.getBooks()

            // Then
            val genesis = books.first { it.id == BookId.GEN }
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
    fun `GIVEN the bundled book files WHEN reading the books THEN starts with nothing read`() = runTest {
        // When
        val books = dataSource.getBooks()

        // Then
        val chapters = books.flatMap { it.chapters }
        assertFalse(books.any { it.isRead })
        assertFalse(chapters.any { it.isRead })
        assertFalse(chapters.flatMap { it.verses }.any { it.isRead })
    }

    @Test
    fun `GIVEN the bundled book files WHEN reading the books THEN has the canonical chapter count`() = runTest {
        // When
        val books = dataSource.getBooks()

        // Then
        assertEquals(1189, books.sumOf { it.chapters.size })
    }
}
