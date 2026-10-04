package com.quare.bibleplanner.core.daystudy.domain.store

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/*
 * Why: prefetched quotas are a head start, never the truth; readers must still refresh
 * in the background.
 */
class DayStudyQuotaPrefetchStore {
    private val quotasByDay = MutableStateFlow<Map<PlanDayLocationModel, DayStudyQuotaModel>>(emptyMap())

    fun put(
        day: PlanDayLocationModel,
        quota: DayStudyQuotaModel,
    ) {
        quotasByDay.update { it + (day to quota) }
    }

    fun findQuota(day: PlanDayLocationModel): DayStudyQuotaModel? = quotasByDay.value[day]
}
