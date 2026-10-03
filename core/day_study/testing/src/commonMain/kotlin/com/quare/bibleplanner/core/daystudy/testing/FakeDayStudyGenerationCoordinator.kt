package com.quare.bibleplanner.core.daystudy.testing

import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeDayStudyGenerationCoordinator(
    pendingOpenKey: String?,
) : DayStudyGenerationCoordinator {
    val jobsFlow = MutableStateFlow<List<DayStudyGenerationJob>>(emptyList())
    val activeKeyFlow = MutableStateFlow<String?>(null)
    val pendingOpenKeyFlow = MutableStateFlow(pendingOpenKey)
    val dismissedKeysFlow = MutableStateFlow<Set<String>>(emptySet())
    val startedJobs = mutableListOf<Triple<List<PassageModel>, DayNavRoute, String>>()
    val startedRewardFlags = mutableListOf<Boolean>()
    val activatedKeys = mutableListOf<String>()
    val clearedKeys = mutableListOf<String>()
    val requestedOpenKeys = mutableListOf<String>()
    val consumedKeys = mutableListOf<String>()
    val dismissedFromCardKeys = mutableListOf<String>()
    val acknowledgedKeys = mutableListOf<String>()
    var generatingCount = 0
    val unservedRewardKeys = mutableSetOf<String>()

    override val jobs: StateFlow<List<DayStudyGenerationJob>> = jobsFlow
    override val activeKey: StateFlow<String?> = activeKeyFlow
    override val pendingOpenKey: StateFlow<String?> = pendingOpenKeyFlow
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
        isRewarded: Boolean,
    ): String {
        startedJobs += Triple(passages, dayRoute, label)
        startedRewardFlags += isRewarded
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

    override fun consumePendingOpen(key: String) {
        consumedKeys += key
        if (pendingOpenKeyFlow.value == key) pendingOpenKeyFlow.value = null
    }

    override fun dismissFromCard(key: String) {
        dismissedFromCardKeys += key
    }

    override fun acknowledge(key: String) {
        acknowledgedKeys += key
    }

    override fun getGeneratingCount(excludingKey: String?): Int = generatingCount

    override fun hasUnservedReward(key: String): Boolean = key in unservedRewardKeys

    private companion object {
        const val KEY_SEPARATOR = "|"
    }
}
