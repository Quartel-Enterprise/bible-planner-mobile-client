package com.quare.bibleplanner.feature.daystudy.domain.store

import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Quotas fetched before anything asked for them, so a screen that opens on a finished day can show
 * its call to action at once instead of waiting on the network. What is kept here is a head start,
 * never the truth: whoever reads it should still refresh in the background.
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
