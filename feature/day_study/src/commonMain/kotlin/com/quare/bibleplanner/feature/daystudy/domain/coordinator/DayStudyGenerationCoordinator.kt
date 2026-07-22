package com.quare.bibleplanner.feature.daystudy.domain.coordinator

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * App-scoped owner of in-flight day-study generations, so a generation survives leaving the day
 * screen. The day screen delegates generation here and observes [jobs] for its own key; a global
 * floating card (rendered at the root) also observes [jobs] to surface background progress.
 *
 * A job's identity is the [DayNavRoute] (one study per day), which both the coordinator and the
 * day screen already hold — no need to resolve the version/language storage key just to track it.
 */
class DayStudyGenerationCoordinator(
    private val applicationScope: ApplicationScope,
    private val getDayStudy: GetDayStudyUseCase,
    private val observeIsProUser: ObserveIsProUser,
    private val trackEvent: TrackEvent,
) {
    private val _jobs: MutableStateFlow<List<DayStudyGenerationJob>> = MutableStateFlow(emptyList())
    val jobs: StateFlow<List<DayStudyGenerationJob>> = _jobs.asStateFlow()

    // The day currently on screen. Its own card/sheet already shows the state, so the global
    // floating card suppresses this key (only backgrounded days show there).
    private val _activeKey: MutableStateFlow<String?> = MutableStateFlow(null)
    val activeKey: StateFlow<String?> = _activeKey.asStateFlow()

    // One-shot request (from the card's "Open") for the arriving day screen to open the study.
    private val _pendingOpenKey: MutableStateFlow<String?> = MutableStateFlow(null)
    val pendingOpenKey: StateFlow<String?> = _pendingOpenKey.asStateFlow()

    // Keys the user dismissed from the card. Generation keeps running (and caching) regardless.
    private val _dismissedKeys: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    val dismissedKeys: StateFlow<Set<String>> = _dismissedKeys.asStateFlow()

    private val generationStartMarks: MutableMap<String, TimeMark> = mutableMapOf()

    fun keyOf(dayRoute: DayNavRoute): String = listOf(
        dayRoute.readingPlanType,
        dayRoute.weekNumber.toString(),
        dayRoute.dayNumber.toString(),
    ).joinToString(KEY_SEPARATOR)

    fun start(
        passages: List<PassageModel>,
        dayRoute: DayNavRoute,
        label: String,
    ): String {
        val key = keyOf(dayRoute)
        if (isGenerating(key)) return key
        _dismissedKeys.update { it - key }
        generationStartMarks[key] = TimeSource.Monotonic.markNow()
        putJob(
            DayStudyGenerationJob(
                key = key,
                label = label,
                dayRoute = dayRoute,
                phase = null,
                status = DayStudyGenerationStatus.Generating,
            ),
        )
        applicationScope.launch {
            suspendRunCatching {
                getDayStudy(passages).collect { event ->
                    when (event) {
                        is DayStudyGenerationEventModel.PhaseChanged -> updateJob(key) { it.copy(phase = event.phase) }

                        is DayStudyGenerationEventModel.Completed -> {
                            updateJob(key) { it.copy(status = DayStudyGenerationStatus.Done(event.study)) }
                            trackGenerationEnd(
                                name = AnalyticsEventNames.DAY_STUDY_GENERATION_COMPLETED,
                                dayRoute = dayRoute,
                            )
                            trackGenerationTime(
                                key = key,
                                reason = null,
                            )
                        }
                    }
                }
            }.onFailure { throwable ->
                val isLimitReached = throwable is LimitReachedException
                updateJob(key) {
                    it.copy(
                        status = DayStudyGenerationStatus.Failed(isLimitReached = isLimitReached),
                    )
                }
                trackGenerationEnd(
                    name = AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED,
                    dayRoute = dayRoute,
                    extraParams = mapOf(
                        AnalyticsParams.REASON to if (isLimitReached) LIMIT_REACHED_REASON else ERROR_REASON,
                    ),
                )
                trackGenerationTime(
                    key = key,
                    reason = if (isLimitReached) LIMIT_REACHED_REASON else ERROR_REASON,
                )
            }
        }
        return key
    }

    fun setActive(key: String) {
        _activeKey.value = key
    }

    fun clearActive(key: String) {
        _activeKey.update { current -> current.takeIf { it != key } }
    }

    fun requestOpen(key: String) {
        _pendingOpenKey.value = key
    }

    fun consumePendingOpen(key: String) {
        _pendingOpenKey.update { current -> current.takeIf { it != key } }
    }

    fun dismissFromCard(key: String) {
        _dismissedKeys.update { it + key }
    }

    fun acknowledge(key: String) {
        _jobs.update { jobs -> jobs.filterNot { it.key == key } }
        _dismissedKeys.update { it - key }
    }

    fun generatingCount(excludingKey: String?): Int = _jobs.value.count { job ->
        job.key != excludingKey && job.status == DayStudyGenerationStatus.Generating
    }

    private fun isGenerating(key: String): Boolean =
        _jobs.value.any { it.key == key && it.status == DayStudyGenerationStatus.Generating }

    private fun putJob(job: DayStudyGenerationJob) {
        _jobs.update { jobs -> jobs.filterNot { it.key == job.key } + job }
    }

    private fun updateJob(
        key: String,
        transform: (DayStudyGenerationJob) -> DayStudyGenerationJob,
    ) {
        _jobs.update { jobs -> jobs.map { job -> if (job.key == key) transform(job) else job } }
    }

    private suspend fun trackGenerationTime(
        key: String,
        reason: String?,
    ) {
        val mark = generationStartMarks.remove(key) ?: return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_GENERATION_TIME,
            params = buildMap {
                put(AnalyticsParams.DURATION_MS, mark.elapsedNow().inWholeMilliseconds)
                put(AnalyticsParams.SUCCESS, reason == null)
                put(AnalyticsParams.IS_PRO, observeIsProUser().first())
                reason?.let { put(AnalyticsParams.REASON, it) }
            },
        )
    }

    private suspend fun trackGenerationEnd(
        name: String,
        dayRoute: DayNavRoute,
        extraParams: Map<String, Any> = emptyMap(),
    ) {
        trackEvent(
            name = name,
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to dayRoute.readingPlanType,
                AnalyticsParams.WEEK_NUMBER to dayRoute.weekNumber,
                AnalyticsParams.DAY_NUMBER to dayRoute.dayNumber,
                AnalyticsParams.IS_PRO to observeIsProUser().first(),
            ) + extraParams,
        )
    }

    private companion object {
        const val KEY_SEPARATOR = "|"
        const val LIMIT_REACHED_REASON = "limit_reached"
        const val ERROR_REASON = "error"
    }
}
