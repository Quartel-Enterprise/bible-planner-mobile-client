package com.quare.bibleplanner.core.daystudy.domain.usecase

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.daystudy.domain.store.DayStudyQuotaPrefetchStore
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.flow.first

class PrefetchDayStudyQuotaUseCase(
    private val getDayPassagesForDayStudy: GetDayPassagesForDayStudyUseCase,
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val quotaPrefetchStore: DayStudyQuotaPrefetchStore,
) : PrefetchDayStudyQuota {
    override suspend fun invoke(day: PlanDayLocationModel) {
        suspendRunCatching {
            val passages = getDayPassagesForDayStudy(
                weekNumber = day.weekNumber,
                dayNumber = day.dayNumber,
                readingPlanType = day.readingPlanType,
            ).first() ?: return@suspendRunCatching
            quotaPrefetchStore.put(
                day = day,
                quota = getDayStudyQuota(passages),
            )
        }.onFailure { throwable ->
            Logger.d(throwable) { "Could not prefetch the day study quota" }
        }
    }
}
