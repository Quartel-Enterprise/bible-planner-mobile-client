package com.quare.bibleplanner.core.books.domain

import com.quare.bibleplanner.core.model.book.BookChapterModel

/**
 * Number of verses in this chapter that count as read. A chapter flagged as read counts as fully
 * read, so reading progress matches the read indicators even when individual verse flags are
 * incomplete.
 */
val BookChapterModel.readVersesCount: Int
    get() = if (isRead) verses.size else verses.count { it.isRead }

/** Whether the verse numbered [verseNumber] in this chapter counts as read. */
fun BookChapterModel.isVerseRead(verseNumber: Int): Boolean =
    isRead || verses.any { it.number == verseNumber && it.isRead }

/**
 * Whether the planned portion of this chapter counts as read: the [startVerse]..[endVerse] range when
 * both bounds are given, everything from [startVerse] onward when only the start is given, or the
 * whole chapter when no range is given.
 */
fun BookChapterModel.isRangeRead(
    startVerse: Int?,
    endVerse: Int?,
): Boolean = when {
    startVerse != null && endVerse != null -> (startVerse..endVerse).all { isVerseRead(it) }
    startVerse != null -> verses.filter { it.number >= startVerse }.all { isVerseRead(it.number) }
    else -> isRead
}
