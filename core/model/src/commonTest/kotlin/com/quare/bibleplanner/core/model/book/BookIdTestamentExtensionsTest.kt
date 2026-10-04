package com.quare.bibleplanner.core.model.book

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BookIdTestamentExtensionsTest {
    private val oldTestamentBooks: List<BookId> = listOf(
        BookId.GEN,
        BookId.EXO,
        BookId.LEV,
        BookId.NUM,
        BookId.DEU,
        BookId.JOS,
        BookId.JDG,
        BookId.RUT,
        BookId.FIRST_SA,
        BookId.SECOND_SA,
        BookId.FIRST_KI,
        BookId.SECOND_KI,
        BookId.FIRST_CH,
        BookId.SECOND_CH,
        BookId.EZR,
        BookId.NEH,
        BookId.EST,
        BookId.JOB,
        BookId.PSA,
        BookId.PRO,
        BookId.ECC,
        BookId.SNG,
        BookId.ISA,
        BookId.JER,
        BookId.LAM,
        BookId.EZK,
        BookId.DAN,
        BookId.HOS,
        BookId.JOL,
        BookId.AMO,
        BookId.OBA,
        BookId.JON,
        BookId.MIC,
        BookId.NAM,
        BookId.HAB,
        BookId.ZEP,
        BookId.HAG,
        BookId.ZEC,
        BookId.MAL,
    )

    private val newTestamentBooks: List<BookId> = listOf(
        BookId.MAT,
        BookId.MRK,
        BookId.LUK,
        BookId.JHN,
        BookId.ACT,
        BookId.ROM,
        BookId.FIRST_CO,
        BookId.SECOND_CO,
        BookId.GAL,
        BookId.EPH,
        BookId.PHP,
        BookId.COL,
        BookId.FIRST_TH,
        BookId.SECOND_TH,
        BookId.FIRST_TI,
        BookId.SECOND_TI,
        BookId.TIT,
        BookId.PHM,
        BookId.HEB,
        BookId.JAS,
        BookId.FIRST_PE,
        BookId.SECOND_PE,
        BookId.FIRST_JN,
        BookId.SECOND_JN,
        BookId.THIRD_JN,
        BookId.JUD,
        BookId.REV,
    )

    @Test
    fun `GIVEN the Old Testament books WHEN counting them THEN there are 39`() {
        // Given
        val books = oldTestamentBooks

        // When
        val count = books.size

        // Then
        assertEquals(39, count)
    }

    @Test
    fun `GIVEN the New Testament books WHEN counting them THEN there are 27`() {
        // Given
        val books = newTestamentBooks

        // When
        val count = books.size

        // Then
        assertEquals(27, count)
    }

    @Test
    fun `GIVEN the Old and New Testament books WHEN joining them THEN they cover every BookId`() {
        // Given
        val books = oldTestamentBooks + newTestamentBooks

        // When
        val coveredBooks = books.toSet()

        // Then
        assertEquals(BookId.entries.toSet(), coveredBooks)
    }

    @Test
    fun `GIVEN every Old Testament book WHEN checking isNewTestament THEN returns false`() {
        // Given
        val books = oldTestamentBooks

        // When
        val isNewTestamentByBook = books.associateWith(BookId::isNewTestament)

        // Then
        isNewTestamentByBook.forEach { (book, isNewTestament) ->
            assertFalse(isNewTestament, "$book should not be in the New Testament")
        }
    }

    @Test
    fun `GIVEN every New Testament book WHEN checking isNewTestament THEN returns true`() {
        // Given
        val books = newTestamentBooks

        // When
        val isNewTestamentByBook = books.associateWith(BookId::isNewTestament)

        // Then
        isNewTestamentByBook.forEach { (book, isNewTestament) ->
            assertTrue(isNewTestament, "$book should be in the New Testament")
        }
    }

    @Test
    fun `GIVEN every Old Testament book WHEN checking isOldTestament THEN returns true`() {
        // Given
        val books = oldTestamentBooks

        // When
        val isOldTestamentByBook = books.associateWith(BookId::isOldTestament)

        // Then
        isOldTestamentByBook.forEach { (book, isOldTestament) ->
            assertTrue(isOldTestament, "$book should be in the Old Testament")
        }
    }

    @Test
    fun `GIVEN every New Testament book WHEN checking isOldTestament THEN returns false`() {
        // Given
        val books = newTestamentBooks

        // When
        val isOldTestamentByBook = books.associateWith(BookId::isOldTestament)

        // Then
        isOldTestamentByBook.forEach { (book, isOldTestament) ->
            assertFalse(isOldTestament, "$book should not be in the Old Testament")
        }
    }

    @Test
    fun `GIVEN Malachi WHEN checking its testament THEN it is the last Old Testament book`() {
        // Given
        val book = BookId.MAL

        // When
        val isOldTestament = book.isOldTestament()
        val isNewTestament = book.isNewTestament()

        // Then
        assertTrue(isOldTestament)
        assertFalse(isNewTestament)
    }

    @Test
    fun `GIVEN Matthew WHEN checking its testament THEN it is the first New Testament book`() {
        // Given
        val book = BookId.MAT

        // When
        val isNewTestament = book.isNewTestament()
        val isOldTestament = book.isOldTestament()

        // Then
        assertTrue(isNewTestament)
        assertFalse(isOldTestament)
    }

    @Test
    fun `GIVEN Revelation WHEN checking its testament THEN it is in the New Testament`() {
        // Given
        val book = BookId.REV

        // When
        val isNewTestament = book.isNewTestament()
        val isOldTestament = book.isOldTestament()

        // Then
        assertTrue(isNewTestament)
        assertFalse(isOldTestament)
    }
}
