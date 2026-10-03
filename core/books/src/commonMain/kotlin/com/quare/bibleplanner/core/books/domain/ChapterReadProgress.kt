package com.quare.bibleplanner.core.books.domain

import com.quare.bibleplanner.core.model.book.BookChapterModel

// Why: a chapter flagged read counts as fully read, so progress matches the read
// indicators even when individual verse flags are incomplete.
val BookChapterModel.readVersesCount: Int
    get() = if (isRead) verses.size else verses.count { it.isRead }

fun BookChapterModel.isVerseRead(verseNumber: Int): Boolean =
    isRead || verses.any { it.number == verseNumber && it.isRead }

fun BookChapterModel.isRangeRead(
    startVerse: Int?,
    endVerse: Int?,
): Boolean = when {
    startVerse != null && endVerse != null -> (startVerse..endVerse).all(::isVerseRead)
    startVerse != null -> verses.filter { it.number >= startVerse }.all { isVerseRead(it.number) }
    else -> isRead
}
