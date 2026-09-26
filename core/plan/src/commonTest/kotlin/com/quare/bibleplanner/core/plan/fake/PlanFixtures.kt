package com.quare.bibleplanner.core.plan.fake

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.WeekPlanModel

internal fun week(
    number: Int,
    days: List<DayModel>,
): WeekPlanModel = WeekPlanModel(
    number = number,
    days = days,
)

internal fun day(
    number: Int,
    passages: List<PassageModel>,
    isRead: Boolean = false,
): DayModel = DayModel(
    number = number,
    passages = passages,
    isRead = isRead,
    totalVerses = 0,
    readVerses = 0,
    readTimestamp = null,
    plannedReadDate = null,
    notes = null,
    isToday = false,
)

internal fun passage(
    bookId: BookId,
    chapters: List<ChapterModel> = emptyList(),
): PassageModel = PassageModel(
    bookId = bookId,
    chapters = chapters,
    isRead = false,
    chapterRanges = null,
)

internal fun chapterPlan(
    bookId: BookId,
    number: Int,
    startVerse: Int? = null,
    endVerse: Int? = null,
): ChapterModel = ChapterModel(
    number = number,
    startVerse = startVerse,
    endVerse = endVerse,
    bookId = bookId,
)

internal fun book(
    bookId: BookId,
    chapters: List<BookChapterModel>,
    isRead: Boolean = false,
): BookDataModel = BookDataModel(
    id = bookId,
    chapters = chapters,
    isRead = isRead,
    isFavorite = false,
)

internal fun bookChapter(
    number: Int,
    verseCount: Int,
    readVerses: Set<Int> = emptySet(),
    isRead: Boolean = false,
): BookChapterModel = BookChapterModel(
    number = number,
    verses = (1..verseCount).map { verseNumber ->
        VerseModel(
            number = verseNumber,
            isRead = verseNumber in readVerses,
        )
    },
    isRead = isRead,
    readUpdatedAt = null,
)
