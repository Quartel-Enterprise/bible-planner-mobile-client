package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel

fun interface GetCompletedDayForChapter {
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
    ): PlanDayLocationModel?
}
