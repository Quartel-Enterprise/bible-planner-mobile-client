package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetTodayListeningDay
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.usecase.GetScheduledDay
import kotlinx.coroutines.flow.first
import kotlinx.datetime.daysUntil

/*
 * Why: the day is worked out from the start date instead of scoring the whole plan, which walks the
 * read state of every book and is far too heavy for opening a chapter.
 */
internal class GetTodayListeningDayUseCase(
    private val planRepository: PlanRepository,
    private val booksRepository: BooksRepository,
    private val getScheduledDay: GetScheduledDay,
    private val dateProvider: ListeningDateProvider,
) : GetTodayListeningDay {
    override suspend fun invoke(chapter: ChapterLocationModel): ListeningDayModel? {
        val startDate = planRepository.getStartPlanTimestamp().first() ?: return null
        val elapsedDays = startDate.daysUntil(dateProvider.today)
        if (elapsedDays < 0) return null
        val readingPlanType = planRepository.getSelectedReadingPlanFlow().first()
        val location = PlanDayLocationModel(
            weekNumber = elapsedDays / DAYS_PER_WEEK + 1,
            dayNumber = elapsedDays % DAYS_PER_WEEK + 1,
            readingPlanType = readingPlanType,
        )
        val day = getScheduledDay(
            weekNumber = location.weekNumber,
            dayNumber = location.dayNumber,
            readingPlanType = readingPlanType,
        ) ?: return null
        val segments = day.passages.flatMap { passage -> toSegments(passage) }
        val isChapterScheduled = segments.any { segment -> segment.chapter == chapter }
        if (!isChapterScheduled || segments.size < MIN_SEGMENTS) return null
        return ListeningDayModel(
            location = location,
            segments = segments,
        )
    }

    private suspend fun toSegments(passage: PassageModel): List<ListeningSegmentModel> =
        if (passage.chapters.isEmpty()) {
            booksRepository
                .getBookByIdFlow(passage.bookId)
                .first()
                ?.chapters
                .orEmpty()
                .map { chapter ->
                    ListeningSegmentModel(
                        chapter = ChapterLocationModel(
                            bookId = passage.bookId,
                            chapterNumber = chapter.number,
                        ),
                        startVerse = null,
                        endVerse = null,
                    )
                }
        } else {
            passage.chapters.map { chapter ->
                ListeningSegmentModel(
                    chapter = ChapterLocationModel(
                        bookId = passage.bookId,
                        chapterNumber = chapter.number,
                    ),
                    startVerse = chapter.startVerse,
                    endVerse = chapter.endVerse,
                )
            }
        }

    private companion object {
        const val DAYS_PER_WEEK = 7
        const val MIN_SEGMENTS = 2
    }
}
