package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.BookId

/**
 * The verses the reader has picked, kept sorted so the reference reads in order.
 *
 * A selection never spans two chapters: vertical reading puts two on screen, and picking a verse in
 * the other one starts over.
 */
data class VerseSelection(
    val bookId: BookId,
    val chapterNumber: Int,
    val verseNumbers: List<Int>,
) {
    val refs: List<VerseRef>
        get() = verseNumbers.map { verseNumber ->
            VerseRef(
                bookId = bookId,
                chapterNumber = chapterNumber,
                verseNumber = verseNumber,
            )
        }
}
