package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.plan.domain.isFullyReadAssuming
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.scheduledDaysFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

/**
 * Answers, ahead of the tap, which of the chapters on screen would complete a plan day the moment
 * they are marked read. A screen that keeps this warm opens the celebration the instant the reader
 * taps, instead of scoring the plan afterwards.
 *
 * Only the books those days schedule are observed — a handful — because the whole-Bible read state
 * is far too heavy to re-read on every change just to answer this.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveDayCompletionCandidatesUseCase(
    private val planRepository: PlanRepository,
    private val booksRepository: BooksRepository,
) : ObserveDayCompletionCandidates {
    override fun invoke(chapters: List<ChapterLocationModel>): Flow<Map<ChapterLocationModel, PlanDayLocationModel>> =
        planRepository
            .getSelectedReadingPlanFlow()
            .flatMapLatest { readingPlanType ->
                val scheduledDays = planRepository
                    .getPlans(readingPlanType)
                    .scheduledDaysFor(
                        chapters = chapters,
                        readingPlanType = readingPlanType,
                    )
                val bookIds = scheduledDays.values
                    .flatMap { scheduled -> scheduled.day.passages.map(PassageModel::bookId) }
                    .distinct()
                if (bookIds.isEmpty()) {
                    flowOf(emptyMap())
                } else {
                    combine(bookIds.map(booksRepository::getBookByIdFlow)) { books ->
                        val knownBooks = books.filterNotNull()
                        scheduledDays
                            .filter { (chapter, scheduled) ->
                                scheduled.day.isFullyReadAssuming(
                                    assumedReadChapter = chapter,
                                    books = knownBooks,
                                )
                            }.mapValues { (_, scheduled) -> scheduled.location }
                    }
                }
            }.flowOn(Dispatchers.Default)
}
