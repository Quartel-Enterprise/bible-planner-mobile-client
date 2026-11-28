package com.quare.bibleplanner.core.books.util

import bibleplanner.core.books.generated.resources.Res
import bibleplanner.core.books.generated.resources.book_first_sa
import bibleplanner.core.books.generated.resources.book_gen
import bibleplanner.core.books.generated.resources.book_rev
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BookNamesTest {
    @Test
    fun `GIVEN the book resources WHEN getting the book names THEN names every book with its own resource`() = runTest {
        // Given
        val expectedNames = mapOf(
            BookId.GEN to getString(Res.string.book_gen),
            BookId.FIRST_SA to getString(Res.string.book_first_sa),
            BookId.REV to getString(Res.string.book_rev),
        )

        // When
        val bookNames = getBookNames()

        // Then
        assertEquals(BookId.entries.toSet(), bookNames.keys)
        assertEquals(expectedNames, bookNames.filterKeys { it in expectedNames })
    }
}
