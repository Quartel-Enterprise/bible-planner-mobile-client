package com.quare.bibleplanner.feature.daystudy.domain.coordinator

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import kotlinx.coroutines.flow.StateFlow

/**
 * App-scoped owner of in-flight day-study generations, so a generation survives leaving the day
 * screen. The day screen delegates generation here and observes [jobs] for its own key; a global
 * floating card (rendered at the root) also observes [jobs] to surface background progress.
 *
 * A job's identity is the [DayNavRoute] (one study per day), which both the coordinator and the
 * day screen already hold — no need to resolve the version/language storage key just to track it.
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
    ): String

    fun setActive(key: String)

    fun clearActive(key: String)

    fun requestOpen(key: String)

    fun consumePendingOpen(key: String)

    fun dismissFromCard(key: String)

    fun acknowledge(key: String)

    fun generatingCount(excludingKey: String?): Int
}
