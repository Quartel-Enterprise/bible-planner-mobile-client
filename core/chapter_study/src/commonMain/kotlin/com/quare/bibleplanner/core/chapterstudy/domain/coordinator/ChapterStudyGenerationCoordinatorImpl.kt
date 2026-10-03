package com.quare.bibleplanner.core.chapterstudy.domain.coordinator

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationEventModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationJob
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationStatus
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GenerateChapterStudy
import com.quare.bibleplanner.core.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

internal class ChapterStudyGenerationCoordinatorImpl(
    private val applicationScope: ApplicationScope,
    private val generateChapterStudy: GenerateChapterStudy,
    private val observeIsProUser: ObserveIsProUser,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    private val isConnected: IsConnected,
    private val trackEvent: TrackEvent,
) : ChapterStudyGenerationCoordinator {
    override val jobs: StateFlow<List<ChapterStudyGenerationJob>>
        field = MutableStateFlow<List<ChapterStudyGenerationJob>>(emptyList())

    private val unservedRewardTargets = MutableStateFlow<Set<ChapterStudyTargetModel>>(emptySet())
    private val connectivityPollInterval: Duration = 3.seconds
    private val generationStartMarks: MutableMap<ChapterStudyTargetModel, TimeMark> = mutableMapOf()

    override fun start(
        target: ChapterStudyTargetModel,
        isRewarded: Boolean,
    ) {
        if (isGenerating(target)) return
        if (isRewarded) unservedRewardTargets.update { it + target }
        generationStartMarks[target] = TimeSource.Monotonic.markNow()
        putJob(
            ChapterStudyGenerationJob(
                target = target,
                phase = null,
                status = ChapterStudyGenerationStatus.Generating,
            ),
        )
        applicationScope.launch {
            val streamJob = launch {
                runGeneration(
                    target = target,
                    isRewarded = isRewarded,
                )
            }
            val connectivityWatcher = launch {
                observeConnectivity().firstOrNull { isOnline -> !isOnline } ?: return@launch
                streamJob.cancel()
            }
            streamJob.join()
            connectivityWatcher.cancel()
            if (streamJob.isCancelled) {
                failGeneration(
                    target = target,
                    isRewarded = isRewarded,
                    isLimitReached = false,
                    isOffline = true,
                )
            }
        }
    }

    override fun acknowledge(target: ChapterStudyTargetModel) {
        jobs.update { currentJobs -> currentJobs.filterNot { it.target == target } }
    }

    override fun getGeneratingCount(excluding: ChapterStudyTargetModel): Int = jobs.value.count { job ->
        job.target != excluding && job.status == ChapterStudyGenerationStatus.Generating
    }

    override fun hasUnservedReward(target: ChapterStudyTargetModel): Boolean = target in unservedRewardTargets.value

    private fun observeConnectivity(): Flow<Boolean> = merge(
        networkConnectivityObserver.observe(),
        flow {
            while (true) {
                delay(connectivityPollInterval)
                emit(isConnected())
            }
        },
    )

    private suspend fun runGeneration(
        target: ChapterStudyTargetModel,
        isRewarded: Boolean,
    ) {
        suspendRunCatching {
            collectGeneration(
                target = target,
                isRewarded = isRewarded,
            )
        }.onFailure { throwable ->
            failGeneration(
                target = target,
                isRewarded = isRewarded,
                isLimitReached = throwable is LimitReachedException,
                isOffline = false,
            )
        }
    }

    private suspend fun collectGeneration(
        target: ChapterStudyTargetModel,
        isRewarded: Boolean,
    ) {
        generateChapterStudy(
            target = target,
            isRewarded = isRewarded,
        ).collect { event ->
            when (event) {
                is ChapterStudyGenerationEventModel.PhaseChanged -> updateJob(target) { it.copy(phase = event.phase) }

                is ChapterStudyGenerationEventModel.Completed -> {
                    unservedRewardTargets.update { it - target }
                    updateJob(target) { it.copy(status = ChapterStudyGenerationStatus.Done(event.study)) }
                    trackGenerationEnd(
                        name = AnalyticsEventNames.CHAPTER_STUDY_GENERATION_COMPLETED,
                        target = target,
                        isRewarded = isRewarded,
                        reason = null,
                    )
                }
            }
        }
    }

    private suspend fun failGeneration(
        target: ChapterStudyTargetModel,
        isRewarded: Boolean,
        isLimitReached: Boolean,
        isOffline: Boolean,
    ) {
        if (isLimitReached) unservedRewardTargets.update { it - target }
        updateJob(target) {
            it.copy(
                status = ChapterStudyGenerationStatus.Failed(
                    isLimitReached = isLimitReached,
                    isOffline = isOffline,
                ),
            )
        }
        trackGenerationEnd(
            name = AnalyticsEventNames.CHAPTER_STUDY_GENERATION_FAILED,
            target = target,
            isRewarded = isRewarded,
            reason = when {
                isLimitReached -> LIMIT_REACHED_REASON
                isOffline -> OFFLINE_REASON
                else -> ERROR_REASON
            },
        )
    }

    private fun isGenerating(target: ChapterStudyTargetModel): Boolean =
        jobs.value.any { it.target == target && it.status == ChapterStudyGenerationStatus.Generating }

    private fun putJob(job: ChapterStudyGenerationJob) {
        jobs.update { currentJobs -> currentJobs.filterNot { it.target == job.target } + job }
    }

    private fun updateJob(
        target: ChapterStudyTargetModel,
        transform: (ChapterStudyGenerationJob) -> ChapterStudyGenerationJob,
    ) {
        jobs.update { currentJobs -> currentJobs.map { job -> if (job.target == target) transform(job) else job } }
    }

    private suspend fun trackGenerationEnd(
        name: String,
        target: ChapterStudyTargetModel,
        isRewarded: Boolean,
        reason: String?,
    ) {
        val durationMs = generationStartMarks.remove(target)?.elapsedNow()?.inWholeMilliseconds
        trackEvent(
            name = name,
            params = buildMap {
                put(AnalyticsParams.BOOK_ID, target.bookId.name)
                put(AnalyticsParams.CHAPTER_NUMBER, target.chapterNumber)
                put(AnalyticsParams.IS_PRO, observeIsProUser().first())
                put(AnalyticsParams.IS_REWARDED, isRewarded)
                durationMs?.let { put(AnalyticsParams.DURATION_MS, it) }
                reason?.let { put(AnalyticsParams.REASON, it) }
            },
        )
    }

    private companion object {
        const val LIMIT_REACHED_REASON = "limit_reached"
        const val ERROR_REASON = "error"
        const val OFFLINE_REASON = "offline"
    }
}
