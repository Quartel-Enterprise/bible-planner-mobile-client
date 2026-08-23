package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import kotlinx.coroutines.flow.Flow

fun interface ObserveDayCompletionCandidates {
    operator fun invoke(chapters: List<ChapterLocationModel>): Flow<Map<ChapterLocationModel, PlanDayLocationModel>>
}
