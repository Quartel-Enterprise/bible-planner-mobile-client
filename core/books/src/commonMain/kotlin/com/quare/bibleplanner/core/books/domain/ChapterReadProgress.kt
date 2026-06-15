package com.quare.bibleplanner.core.books.domain

import com.quare.bibleplanner.core.model.book.BookChapterModel

/*
 * Single source of truth for what counts as "read" inside a chapter.
 *
 * A chapter flagged as read (BookChapterModel.isRead, itself derived from the stored chapter flag OR
 * all of its verses being read) is treated as having ALL its verses read, even when the individual
 * verse flags are incomplete. Read state syncs at chapter granularity and legacy data may carry the
 * chapter flag without every verse flag, so deriving every read count and completion check from this
 * rule keeps progress consistent with the read indicators and identical across devices.
 */

/** Number of verses in this chapter that count as read. */
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
