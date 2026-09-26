package com.quare.bibleplanner.core.books.data.provider

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.isNewTestament
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BookMapsProviderTest {
    private lateinit var bookMaps: List<Map<String, BookId>>

    @BeforeTest
    fun setUp() {
        bookMaps = BookMapsProvider().bookMaps
    }

    @Test
    fun `WHEN listing the book maps THEN covers every book once in canonical order`() {
        // When
        val bookIds = bookMaps.flatMap { it.values }

        // Then
        assertEquals(BookId.entries, bookIds)
    }

    @Test
    fun `WHEN listing the book maps THEN groups them into five old and five new testament groups`() {
        // When
        val testamentOfEachGroup = bookMaps.map { group -> group.values.map { it.isNewTestament() }.distinct() }

        // Then
        assertEquals(
            List(5) { listOf(false) } + List(5) { listOf(true) },
            testamentOfEachGroup,
        )
    }

    @Test
    fun `WHEN resolving numbered book codes THEN maps them to their ordinal book ids`() {
        // When
        val merged = bookMaps.reduce { acc, group -> acc + group }

        // Then
        assertEquals(BookId.FIRST_SA, merged["1SA"])
        assertEquals(BookId.THIRD_JN, merged["3JN"])
        assertEquals(BookId.SNG, merged["SNG"])
    }
}
