package com.quare.bibleplanner.feature.day.fake

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakeDayStudyGenerationCoordinator(
    pendingOpenKey: String?,
) : DayStudyGenerationCoordinator {
    val consumedKeys = mutableListOf<String>()
    override val jobs: StateFlow<List<DayStudyGenerationJob>> = MutableStateFlow(emptyList())
    override val activeKey: StateFlow<String?> = MutableStateFlow(null)
    override val pendingOpenKey = MutableStateFlow(pendingOpenKey)
    override val dismissedKeys: StateFlow<Set<String>> = MutableStateFlow(emptySet())

    override fun keyOf(dayRoute: DayNavRoute): String =
        "${dayRoute.readingPlanType}-${dayRoute.weekNumber}-${dayRoute.dayNumber}"

    override fun start(
        passages: List<PassageModel>,
        dayRoute: DayNavRoute,
        label: String,
    ): String = error("unused")

    override fun setActive(key: String) = error("unused")

    override fun clearActive(key: String) = error("unused")

    override fun requestOpen(key: String) = error("unused")

    override fun consumePendingOpen(key: String) {
        consumedKeys += key
        pendingOpenKey.value = null
    }

    override fun dismissFromCard(key: String) = error("unused")

    override fun acknowledge(key: String) = error("unused")

    override fun getGeneratingCount(excludingKey: String?): Int = error("unused")
}
