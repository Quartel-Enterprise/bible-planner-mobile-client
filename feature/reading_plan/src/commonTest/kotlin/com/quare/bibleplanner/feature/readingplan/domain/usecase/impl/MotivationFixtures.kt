package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal fun passage(
    bookId: BookId = BookId.GEN,
    isRead: Boolean = false,
): PassageModel = PassageModel(
    bookId = bookId,
    chapters = listOf(
        ChapterModel(number = 1, startVerse = null, endVerse = null, bookId = bookId),
    ),
    isRead = isRead,
    chapterRanges = null,
)

internal fun day(
    number: Int = 1,
    isRead: Boolean = false,
    isToday: Boolean = false,
    readVerses: Int = 0,
    totalVerses: Int = 10,
    plannedReadDate: LocalDate? = null,
    readTimestamp: Long? = null,
    passages: List<PassageModel> = listOf(passage(isRead = isRead)),
): DayModel = DayModel(
    number = number,
    passages = passages,
    isRead = isRead,
    totalVerses = totalVerses,
    readVerses = readVerses,
    readTimestamp = readTimestamp,
    plannedReadDate = plannedReadDate,
    notes = null,
    isToday = isToday,
)

internal fun LocalDate.toEpochMillisLocal(): Long =
    atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
