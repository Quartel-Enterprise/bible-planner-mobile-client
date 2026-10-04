package com.quare.bibleplanner.core.daystudy.domain.coordinator

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import kotlinx.coroutines.flow.StateFlow

/*
 * Why: app-scoped so a generation survives leaving the day screen (a root floating card also
 * observes jobs); a job is keyed by its DayNavRoute, one study per day, so no storage key lookup.
 */
interface DayStudyGenerationCoordinator {
    val jobs: StateFlow<List<DayStudyGenerationJob>>
    val activeKey: StateFlow<String?>
    val pendingOpenKey: StateFlow<String?>
    val dismissedKeys: StateFlow<Set<String>>

    fun keyOf(dayRoute: DayNavRoute): String

    fun start(
        passages: List<PassageModel>,
        dayRoute: DayNavRoute,
        label: String,
        isRewarded: Boolean,
    ): String

    fun setActive(key: String)

    fun clearActive(key: String)

    fun requestOpen(key: String)

    fun consumePendingOpen(key: String)

    fun dismissFromCard(key: String)

    fun acknowledge(key: String)

    fun getGeneratingCount(excludingKey: String?): Int

    fun hasUnservedReward(key: String): Boolean
}
