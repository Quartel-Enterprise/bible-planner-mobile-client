package com.quare.bibleplanner.feature.daystudy.fake

import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakeDayStudyGenerationCoordinator : DayStudyGenerationCoordinator {
    val jobsFlow = MutableStateFlow<List<DayStudyGenerationJob>>(emptyList())
    val activeKeyFlow = MutableStateFlow<String?>(null)
    val dismissedKeysFlow = MutableStateFlow<Set<String>>(emptySet())
    val requestedOpenKeys = mutableListOf<String>()
    val dismissedFromCardKeys = mutableListOf<String>()
    val activatedKeys = mutableListOf<String>()
    val clearedKeys = mutableListOf<String>()
    val acknowledgedKeys = mutableListOf<String>()
    val startedJobs = mutableListOf<Triple<List<PassageModel>, DayNavRoute, String>>()
    var generatingCount = 0

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
    ): String {
        startedJobs += Triple(passages, dayRoute, label)
        return keyOf(dayRoute)
    }

    override fun setActive(key: String) {
        activatedKeys += key
    }

    override fun clearActive(key: String) {
        clearedKeys += key
    }

    override fun requestOpen(key: String) {
        requestedOpenKeys += key
    }

    override fun consumePendingOpen(key: String) = Unit

    override fun dismissFromCard(key: String) {
        dismissedFromCardKeys += key
    }

    override fun acknowledge(key: String) {
        acknowledgedKeys += key
    }

    override fun getGeneratingCount(excludingKey: String?): Int = generatingCount

    private companion object {
        const val KEY_SEPARATOR = "|"
    }
}
