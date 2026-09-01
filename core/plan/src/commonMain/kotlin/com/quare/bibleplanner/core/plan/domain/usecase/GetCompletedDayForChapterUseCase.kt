package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.plan.domain.findCompletedDayFor
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.first

/**
 * The plan day a chapter belongs to, and only while that whole day reads as done — so a caller can
 * celebrate a finished day without knowing which day, or even which plan, the chapter came from.
 * Only the selected plan is considered, because that is the plan the reader follows.
 */
class GetCompletedDayForChapterUseCase(
    private val getPlansByWeekUseCase: GetPlansByWeekUseCase,
    private val planRepository: PlanRepository,
) : GetCompletedDayForChapter {
    override suspend fun invoke(
        bookId: BookId,
        chapterNumber: Int,
    ): PlanDayLocationModel? {
        val readingPlanType = planRepository.getSelectedReadingPlanFlow().first()
        return getPlansByWeekUseCase(readingPlanType)
            .first()
            .findCompletedDayFor(
                bookId = bookId,
                chapterNumber = chapterNumber,
                readingPlanType = readingPlanType,
            )
    }
}
