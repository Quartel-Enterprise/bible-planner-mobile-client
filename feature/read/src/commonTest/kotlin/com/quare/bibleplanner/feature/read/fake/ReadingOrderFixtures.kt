package com.quare.bibleplanner.feature.read.fake

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.WeekPlanModel

internal fun passage(
    bookId: BookId,
    vararg chapterNumbers: Int,
): PassageModel = PassageModel(
    bookId = bookId,
    chapters = chapterNumbers.map { number ->
        ChapterModel(
            number = number,
            startVerse = null,
            endVerse = null,
            bookId = bookId,
        )
    },
    isRead = false,
    chapterRanges = null,
)

internal fun singleWeek(vararg dayPassages: List<PassageModel>): List<WeekPlanModel> = listOf(
    WeekPlanModel(
        number = 1,
        days = dayPassages.mapIndexed { index, passages ->
            DayModel(
                number = index + 1,
                passages = passages,
                isRead = false,
                totalVerses = 0,
                readVerses = 0,
                readTimestamp = null,
                plannedReadDate = null,
                notes = null,
                isToday = false,
            )
        },
    ),
)

internal fun book(
    bookId: BookId,
    chapterCount: Int,
): BookDataModel = BookDataModel(
    id = bookId,
    chapters = (1..chapterCount).map { number ->
        BookChapterModel(
            number = number,
            verses = emptyList(),
            isRead = false,
            readUpdatedAt = null,
        )
    },
    isRead = false,
    isFavorite = false,
)
