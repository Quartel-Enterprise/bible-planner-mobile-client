package com.quare.bibleplanner.core.provider.room.db

import com.quare.bibleplanner.core.model.book.BookId

data class VerseCountCorrection(
    val bookId: BookId,
    val chapter: Int,
    val verses: Int,
)

// Why: each count is the highest verse number any of the nine versions in Storage uses, and
// mirrors the corrected books_by_chapter seed files so migrated devices match fresh installs.
internal val VERSE_COUNT_CORRECTIONS: List<VerseCountCorrection> = listOf(
    VerseCountCorrection(bookId = BookId.GEN, chapter = 34, verses = 31),
    VerseCountCorrection(bookId = BookId.GEN, chapter = 35, verses = 29),
    VerseCountCorrection(bookId = BookId.GEN, chapter = 36, verses = 43),
    VerseCountCorrection(bookId = BookId.GEN, chapter = 37, verses = 36),
    VerseCountCorrection(bookId = BookId.GEN, chapter = 38, verses = 30),
    VerseCountCorrection(bookId = BookId.GEN, chapter = 40, verses = 23),
    VerseCountCorrection(bookId = BookId.GEN, chapter = 41, verses = 57),
    VerseCountCorrection(bookId = BookId.GEN, chapter = 42, verses = 38),
    VerseCountCorrection(bookId = BookId.GEN, chapter = 44, verses = 34),
    VerseCountCorrection(bookId = BookId.GEN, chapter = 45, verses = 28),
    VerseCountCorrection(bookId = BookId.DEU, chapter = 12, verses = 32),
    VerseCountCorrection(bookId = BookId.DEU, chapter = 13, verses = 18),
    VerseCountCorrection(bookId = BookId.JDG, chapter = 5, verses = 32),
    VerseCountCorrection(bookId = BookId.FIRST_SA, chapter = 20, verses = 43),
    VerseCountCorrection(bookId = BookId.FIRST_KI, chapter = 22, verses = 54),
    VerseCountCorrection(bookId = BookId.SECOND_CH, chapter = 13, verses = 22),
    VerseCountCorrection(bookId = BookId.SECOND_CH, chapter = 14, verses = 15),
    VerseCountCorrection(bookId = BookId.PSA, chapter = 47, verses = 10),
    VerseCountCorrection(bookId = BookId.PSA, chapter = 108, verses = 13),
    VerseCountCorrection(bookId = BookId.PSA, chapter = 140, verses = 13),
    VerseCountCorrection(bookId = BookId.PSA, chapter = 142, verses = 7),
    VerseCountCorrection(bookId = BookId.ISA, chapter = 64, verses = 12),
    VerseCountCorrection(bookId = BookId.JER, chapter = 8, verses = 22),
    VerseCountCorrection(bookId = BookId.JER, chapter = 9, verses = 26),
    VerseCountCorrection(bookId = BookId.DAN, chapter = 3, verses = 30),
    VerseCountCorrection(bookId = BookId.DAN, chapter = 4, verses = 37),
    VerseCountCorrection(bookId = BookId.DAN, chapter = 5, verses = 31),
    VerseCountCorrection(bookId = BookId.ROM, chapter = 14, verses = 26),
    VerseCountCorrection(bookId = BookId.REV, chapter = 12, verses = 18),
)
