package com.quare.bibleplanner.feature.bookdetails.presentation.utils

import bibleplanner.feature.book_details.generated.resources.Res
import bibleplanner.feature.book_details.generated.resources.bible_book_1_chronicles_synopsis
import bibleplanner.feature.book_details.generated.resources.bible_book_genesis_synopsis
import bibleplanner.feature.book_details.generated.resources.bible_book_revelation_synopsis
import com.quare.bibleplanner.core.model.book.BookId
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BookSynopsisResourceMapperTest {
    @Test
    fun `GIVEN every book WHEN mapping to a synopsis THEN each book gets its own synopsis`() {
        // Given
        val books = BookId.entries

        // When
        val synopses = books.map(BookId::toSynopsisResource)

        // Then
        assertEquals(books.size, synopses.toSet().size)
    }

    @Test
    fun `GIVEN genesis WHEN mapping to a synopsis THEN returns the genesis synopsis`() {
        // When
        val synopsis = BookId.GEN.toSynopsisResource()

        // Then
        assertEquals(Res.string.bible_book_genesis_synopsis, synopsis)
    }

    @Test
    fun `GIVEN a numbered book WHEN mapping to a synopsis THEN returns the synopsis of that numbered book`() {
        // When
        val synopsis = BookId.FIRST_CH.toSynopsisResource()

        // Then
        assertEquals(Res.string.bible_book_1_chronicles_synopsis, synopsis)
    }

    @Test
    fun `GIVEN revelation WHEN mapping to a synopsis THEN returns the revelation synopsis`() {
        // When
        val synopsis = BookId.REV.toSynopsisResource()

        // Then
        assertEquals(Res.string.bible_book_revelation_synopsis, synopsis)
    }
}
