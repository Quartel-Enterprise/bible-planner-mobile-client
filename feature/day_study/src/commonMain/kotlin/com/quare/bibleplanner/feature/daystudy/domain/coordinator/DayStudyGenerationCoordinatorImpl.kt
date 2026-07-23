package com.quare.bibleplanner.feature.daystudy.domain.coordinator

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class DayStudyGenerationCoordinatorImpl(
    private val applicationScope: ApplicationScope,
    private val getDayStudy: GetDayStudyUseCase,
    private val observeIsProUser: ObserveIsProUser,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    private val isConnected: IsConnected,
    private val trackEvent: TrackEvent,
) : DayStudyGenerationCoordinator {
    private val _jobs: MutableStateFlow<List<DayStudyGenerationJob>> = MutableStateFlow(emptyList())
    override val jobs: StateFlow<List<DayStudyGenerationJob>> = _jobs.asStateFlow()

    // The day currently on screen. Its own card/sheet already shows the state, so the global
    // floating card suppresses this key (only backgrounded days show there).
    private val _activeKey: MutableStateFlow<String?> = MutableStateFlow(null)
    override val activeKey: StateFlow<String?> = _activeKey.asStateFlow()

    // One-shot request (from the card's "Open") for the arriving day screen to open the study.
    private val _pendingOpenKey: MutableStateFlow<String?> = MutableStateFlow(null)
    override val pendingOpenKey: StateFlow<String?> = _pendingOpenKey.asStateFlow()

    // Keys the user dismissed from the card. Generation keeps running (and caching) regardless.
    private val _dismissedKeys: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    override val dismissedKeys: StateFlow<Set<String>> = _dismissedKeys.asStateFlow()

    private val connectivityPollInterval: Duration = 3.seconds
    private val generationStartMarks: MutableMap<String, TimeMark> = mutableMapOf()
    private val phaseEntriesByKey: MutableMap<String, MutableMap<DayStudyPhaseModel, Long>> = mutableMapOf()
    private val logger = Logger.withTag(PERF_LOG_TAG)

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
            val streamJob = launch { runGeneration(key = key, passages = passages, dayRoute = dayRoute) }
            val connectivityWatcher = launch {
                connectivitySignals().firstOrNull { isOnline -> !isOnline } ?: return@launch
                streamJob.cancel()
                failGeneration(
                    key = key,
                    dayRoute = dayRoute,
                    isLimitReached = false,
                    isOffline = true,
                )
            }
            streamJob.join()
            connectivityWatcher.cancel()
        }
        return key
    }

    private fun connectivitySignals(): Flow<Boolean> = merge(
        networkConnectivityObserver.observe(),
        flow {
            while (true) {
                delay(connectivityPollInterval)
                emit(isConnected())
            }
        },
    )

    private suspend fun runGeneration(
        key: String,
        passages: List<PassageModel>,
        dayRoute: DayNavRoute,
    ) {
        suspendRunCatching {
            collectGeneration(
                key = key,
                dayRoute = dayRoute,
                passages = passages,
            )
        }.onFailure { throwable ->
            failGeneration(
                key = key,
                dayRoute = dayRoute,
                isLimitReached = throwable is LimitReachedException,
                isOffline = false,
            )
        }
    }

    private suspend fun collectGeneration(
        key: String,
        dayRoute: DayNavRoute,
        passages: List<PassageModel>,
    ) {
        try {
            getDayStudy(passages).collect { event ->
                when (event) {
                    is DayStudyGenerationEventModel.PhaseChanged -> {
                        recordPhaseEntry(
                            key = key,
                            phase = event.phase,
                        )
                        updateJob(key) { it.copy(phase = event.phase) }
                    }

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
        } catch (timeout: TimeoutCancellationException) {
            throw IllegalStateException("Day study stream stalled without events", timeout)
        }
    }

    private suspend fun failGeneration(
        key: String,
        dayRoute: DayNavRoute,
        isLimitReached: Boolean,
        isOffline: Boolean,
    ) {
        val reason = when {
            isLimitReached -> LIMIT_REACHED_REASON
            isOffline -> OFFLINE_REASON
            else -> ERROR_REASON
        }
        updateJob(key) {
            it.copy(
                status = DayStudyGenerationStatus.Failed(
                    isLimitReached = isLimitReached,
                    isOffline = isOffline,
                ),
            )
        }
        trackGenerationEnd(
            name = AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED,
            dayRoute = dayRoute,
            extraParams = mapOf(AnalyticsParams.REASON to reason),
        )
        trackGenerationTime(
            key = key,
            reason = reason,
        )
    }

    override fun setActive(key: String) {
        _activeKey.value = key
    }

    override fun clearActive(key: String) {
        _activeKey.update { current -> current.takeIf { it != key } }
    }

    override fun requestOpen(key: String) {
        _pendingOpenKey.value = key
    }

    override fun consumePendingOpen(key: String) {
        _pendingOpenKey.update { current -> current.takeIf { it != key } }
    }

    override fun dismissFromCard(key: String) {
        _dismissedKeys.update { it + key }
    }

    override fun acknowledge(key: String) {
        _jobs.update { jobs -> jobs.filterNot { it.key == key } }
        _dismissedKeys.update { it - key }
    }

    override fun generatingCount(excludingKey: String?): Int = _jobs.value.count { job ->
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

    private fun recordPhaseEntry(
        key: String,
        phase: DayStudyPhaseModel,
    ) {
        val mark = generationStartMarks[key] ?: return
        phaseEntriesByKey
            .getOrPut(key, ::mutableMapOf)
            .getOrPut(phase) { mark.elapsedNow().inWholeMilliseconds }
    }

    private suspend fun trackGenerationTime(
        key: String,
        reason: String?,
    ) {
        val mark = generationStartMarks.remove(key) ?: return
        val durationMs = mark.elapsedNow().inWholeMilliseconds
        val phaseDurations = phaseDurations(
            entries = phaseEntriesByKey.remove(key).orEmpty(),
            totalMs = durationMs,
        )
        logger.d {
            "day_study_generation_time key=$key durationMs=$durationMs success=${reason == null} " +
                "reason=$reason phases=$phaseDurations"
        }
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_GENERATION_TIME,
            params = buildMap {
                put(AnalyticsParams.DURATION_MS, durationMs)
                put(AnalyticsParams.SUCCESS, reason == null)
                put(AnalyticsParams.IS_PRO, observeIsProUser().first())
                reason?.let { put(AnalyticsParams.REASON, it) }
                putAll(phaseDurations)
            },
        )
    }

    private fun phaseDurations(
        entries: Map<DayStudyPhaseModel, Long>,
        totalMs: Long,
    ): Map<String, Long> {
        val ordered = entries.entries.sortedBy { it.value }
        return ordered
            .mapIndexed { index, entry ->
                val end = ordered.getOrNull(index + 1)?.value ?: totalMs
                entry.key.toDurationParam() to (end - entry.value)
            }.toMap()
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
        const val OFFLINE_REASON = "offline"
        const val PERF_LOG_TAG = "DayStudyPerf"
    }
}

private fun DayStudyPhaseModel.toDurationParam(): String = when (this) {
    DayStudyPhaseModel.READING -> AnalyticsParams.READING_MS
    DayStudyPhaseModel.CHAPTERS -> AnalyticsParams.CHAPTERS_MS
    DayStudyPhaseModel.CONTEXT -> AnalyticsParams.CONTEXT_MS
    DayStudyPhaseModel.QUESTIONS -> AnalyticsParams.QUESTIONS_MS
}
