package com.quare.bibleplanner.core.books.domain

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterModel

/*
 * Why: a chapter flagged read counts as fully read, so progress matches the read
 * indicators even when individual verse flags are incomplete.
 */
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

/*
 * Why: a book that isn't stored yet, or a chapter it doesn't hold, has nothing read, so the plan
 * screens report it unread instead of failing.
 */
fun BookDataModel?.isChapterRead(chapter: ChapterModel): Boolean = this
    ?.chapters
    ?.find { it.number == chapter.number }
    ?.isRangeRead(
        startVerse = chapter.startVerse,
        endVerse = chapter.endVerse,
    ) == true
