package com.quare.bibleplanner.feature.daystudy.domain.coordinator

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakeDayStudyGenerationCoordinator : DayStudyGenerationCoordinator {
    val jobsFlow = MutableStateFlow<List<DayStudyGenerationJob>>(emptyList())
    val activeKeyFlow = MutableStateFlow<String?>(null)
    val dismissedKeysFlow = MutableStateFlow<Set<String>>(emptySet())
    val requestedOpenKeys = mutableListOf<String>()
    val dismissedFromCardKeys = mutableListOf<String>()

    override val jobs: StateFlow<List<DayStudyGenerationJob>> = jobsFlow
    override val activeKey: StateFlow<String?> = activeKeyFlow
    override val pendingOpenKey: StateFlow<String?> = MutableStateFlow(null)
    override val dismissedKeys: StateFlow<Set<String>> = dismissedKeysFlow

    override fun keyOf(dayRoute: DayNavRoute): String = listOf(
        dayRoute.readingPlanType,
        dayRoute.weekNumber.toString(),
        dayRoute.dayNumber.toString(),
    ).joinToString(KEY_SEPARATOR)

    override fun start(
        passages: List<PassageModel>,
        dayRoute: DayNavRoute,
        label: String,
    ): String = keyOf(dayRoute)

    override fun setActive(key: String) = Unit

    override fun clearActive(key: String) = Unit

    override fun requestOpen(key: String) {
        requestedOpenKeys += key
    }

    override fun consumePendingOpen(key: String) = Unit

    override fun dismissFromCard(key: String) {
        dismissedFromCardKeys += key
    }

    override fun acknowledge(key: String) = Unit

    override fun generatingCount(excludingKey: String?): Int = 0

    private companion object {
        const val KEY_SEPARATOR = "|"
    }
}
