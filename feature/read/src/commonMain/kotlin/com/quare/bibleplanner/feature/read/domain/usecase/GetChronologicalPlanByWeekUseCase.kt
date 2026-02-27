package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import kotlinx.coroutines.flow.Flow

class GetReadNavigationSuggestionsModelUseCase(
    private val getPlansByWeek: GetPlansByWeekUseCase
) {
    /**
     * Get read navigation suggestions model
     * @param shouldForceChronologicalOrder if true we should get the model from the chronological order, if not we should verify the selected plan
     * @param currentBookId the book id reference to calculate next and previous
     * @param currentChapterNumber the chapter number reference to calculate next and previous
     */
    operator fun invoke(
        shouldForceChronologicalOrder: Boolean,
        currentBookId: BookId,
        currentChapterNumber: Int,
    ): Flow<ReadNavigationSuggestionsModel> = TODO()
}
