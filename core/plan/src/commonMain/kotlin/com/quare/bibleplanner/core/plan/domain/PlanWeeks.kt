package com.quare.bibleplanner.core.plan.domain

import com.quare.bibleplanner.core.books.domain.isRangeRead
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.model.ScheduledPlanDayModel

/**
 * The first day of the plan that both schedules this chapter and is now fully read. A chapter can be
 * scheduled more than once, so plan order decides — the earlier day is the one the reader is
 * catching up on.
 */
internal fun List<WeekPlanModel>.findCompletedDayFor(
    bookId: BookId,
    chapterNumber: Int,
    readingPlanType: ReadingPlanType,
): PlanDayLocationModel? = firstNotNullOfOrNull { week ->
    week.days
        .find { day -> day.isRead && day.hasChapter(bookId, chapterNumber) }
        ?.let { day ->
            PlanDayLocationModel(
                weekNumber = week.number,
                dayNumber = day.number,
                readingPlanType = readingPlanType,
            )
        }
}

/** The day each chapter is scheduled in, keeping the plan order for chapters scheduled twice. */
internal fun List<WeekPlanModel>.scheduledDaysFor(
    chapters: List<ChapterLocationModel>,
    readingPlanType: ReadingPlanType,
): Map<ChapterLocationModel, ScheduledPlanDayModel> = chapters
    .mapNotNull { chapter ->
        firstNotNullOfOrNull { week ->
            week.days
                .find { day -> day.hasChapter(chapter.bookId, chapter.chapterNumber) }
                ?.let { day ->
                    chapter to ScheduledPlanDayModel(
                        location = PlanDayLocationModel(
                            weekNumber = week.number,
                            dayNumber = day.number,
                            readingPlanType = readingPlanType,
                        ),
                        day = day,
                    )
                }
        }
    }.toMap()

/**
 * Whether the day would count as fully read once [assumedReadChapter] is marked read — the question
 * a screen asks before the write, so it already knows what the tap will mean.
 */
internal fun DayModel.isFullyReadAssuming(
    assumedReadChapter: ChapterLocationModel,
    books: List<BookDataModel>,
): Boolean = passages.all { passage ->
    val book = books.find { it.id == passage.bookId } ?: return@all false
    val isAssumedBook = passage.bookId == assumedReadChapter.bookId
    if (passage.chapters.isEmpty()) {
        book.chapters.all { chapter ->
            chapter.isRead || (isAssumedBook && chapter.number == assumedReadChapter.chapterNumber)
        }
    } else {
        passage.chapters.all { chapterPlan ->
            if (isAssumedBook && chapterPlan.number == assumedReadChapter.chapterNumber) {
                true
            } else {
                book.chapters
                    .find { it.number == chapterPlan.number }
                    ?.isRangeRead(chapterPlan.startVerse, chapterPlan.endVerse) == true
            }
        }
    }
}

private fun DayModel.hasChapter(
    bookId: BookId,
    chapterNumber: Int,
): Boolean = passages.any { passage ->
    passage.bookId == bookId &&
        (passage.chapters.isEmpty() || passage.chapters.any { it.number == chapterNumber })
}
