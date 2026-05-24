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
    fun `there are 39 Old Testament books`() {
        assertEquals(39, oldTestamentBooks.size)
    }

    @Test
    fun `there are 27 New Testament books`() {
        assertEquals(27, newTestamentBooks.size)
    }

    @Test
    fun `OT and NT lists together cover every BookId`() {
        assertEquals(BookId.entries.toSet(), (oldTestamentBooks + newTestamentBooks).toSet())
    }

    @Test
    fun `every Old Testament book returns false for isNewTestament`() {
        oldTestamentBooks.forEach { book ->
            assertFalse(book.isNewTestament(), "$book should not be in the New Testament")
        }
    }

    @Test
    fun `every New Testament book returns true for isNewTestament`() {
        newTestamentBooks.forEach { book ->
            assertTrue(book.isNewTestament(), "$book should be in the New Testament")
        }
    }

    @Test
    fun `every Old Testament book returns true for isOldTestament`() {
        oldTestamentBooks.forEach { book ->
            assertTrue(book.isOldTestament(), "$book should be in the Old Testament")
        }
    }

    @Test
    fun `every New Testament book returns false for isOldTestament`() {
        newTestamentBooks.forEach { book ->
            assertFalse(book.isOldTestament(), "$book should not be in the Old Testament")
        }
    }

    @Test
    fun `Malachi is the last Old Testament book`() {
        assertTrue(BookId.MAL.isOldTestament())
        assertFalse(BookId.MAL.isNewTestament())
    }

    @Test
    fun `Matthew is the first New Testament book`() {
        assertTrue(BookId.MAT.isNewTestament())
        assertFalse(BookId.MAT.isOldTestament())
    }

    @Test
    fun `Revelation is in the New Testament`() {
        assertTrue(BookId.REV.isNewTestament())
        assertFalse(BookId.REV.isOldTestament())
    }
}
