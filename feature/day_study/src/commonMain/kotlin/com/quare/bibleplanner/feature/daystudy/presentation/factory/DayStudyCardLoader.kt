package com.quare.bibleplanner.feature.daystudy.presentation.factory

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// Why: a factory, so each ViewModel gets its own instance and the load timing of one never ends another's.
internal class DayStudyCardLoader(
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val hasCachedStudy: HasCachedStudyUseCase,
    private val observeIsProUser: ObserveIsProUser,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val cardUiModelFactory: DayStudyCardUiModelFactory,
    private val trackEvent: TrackEvent,
) {
    /*
     * Why: the Pro state the card was last loaded with. The generation gate and its analytics read it here, so they
     * agree with the card on screen; a factory binding keeps it per ViewModel.
     */
    var isPro: Boolean = false
        private set
    private var loadStartMark: TimeMark? = null
    private var loadTarget: String = ""
    private val logger = Logger.withTag(PERF_LOG_TAG)

    fun startLoad(target: String) {
        loadTarget = target
        loadStartMark = TimeSource.Monotonic.markNow()
    }

    /*
     * Why: the card depends on who is signed in too, so a new user with the same Pro state reloads it.
     * A failed load emits nothing: the caller keeps the card it shows. The passages are read on every load, since
     * the caller's can change after collection starts.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(
        passages: () -> List<PassageModel>,
        useCache: Boolean,
    ): Flow<DayStudyCardUiModel> = flow {
        if (useCache) {
            loadCached(
                passages = passages(),
                isPro = isPro,
            )?.let { emit(it) }
        }
        emitAll(
            combine(
                observeAuthenticatedUserId(),
                observeIsProUser(),
            ) { userId, isPro -> userId to isPro }
                .distinctUntilChanged()
                .transformLatest { (_, isPro) ->
                    this@DayStudyCardLoader.isPro = isPro
                    load(
                        passages = passages(),
                        isPro = isPro,
                    )?.let { emit(it) }
                },
        )
    }

    private suspend fun loadCached(
        passages: List<PassageModel>,
        isPro: Boolean,
    ): DayStudyCardUiModel? {
        if (!hasCachedStudy(passages)) return null
        trackLoad(
            isPro = isPro,
            isCached = true,
            reason = null,
        )
        return cardUiModelFactory.createFromCache(isPro = isPro)
    }

    suspend fun reload(passages: List<PassageModel>): DayStudyCardUiModel? = load(
        passages = passages,
        isPro = isPro,
    )

    private suspend fun load(
        passages: List<PassageModel>,
        isPro: Boolean,
    ): DayStudyCardUiModel? = suspendRunCatching { getDayStudyQuota(passages) }
        .onSuccess { quota ->
            trackLoad(
                isPro = isPro,
                isCached = quota.hasLocalStudy,
                reason = null,
            )
        }.onFailure { throwable ->
            trackLoad(
                isPro = isPro,
                isCached = false,
                reason = throwable::class.simpleName ?: UNKNOWN_REASON,
            )
        }.getOrNull()
        ?.let { quota ->
            cardUiModelFactory.create(
                isPro = isPro,
                quota = quota,
            )
        }

    suspend fun loadLocked(
        card: DayStudyCardUiModel,
        passages: List<PassageModel>,
    ): DayStudyCardUiModel {
        val rewardedRemainingToday = suspendRunCatching { getDayStudyQuota(passages).rewardedRemainingToday }
            .getOrDefault(0)
        return cardUiModelFactory.createLocked(
            card = card,
            rewardedRemainingToday = rewardedRemainingToday,
        )
    }

    private fun trackLoad(
        isPro: Boolean,
        isCached: Boolean,
        reason: String?,
    ) {
        val mark = loadStartMark ?: return
        loadStartMark = null
        val durationMs = mark.elapsedNow().inWholeMilliseconds
        logger.d {
            "day_study_load target=$loadTarget durationMs=$durationMs success=${reason == null} " +
                "isCached=$isCached isPro=$isPro reason=$reason"
        }
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_LOAD,
            params = buildMap {
                put(AnalyticsParams.TARGET, loadTarget)
                put(AnalyticsParams.DURATION_MS, durationMs)
                put(AnalyticsParams.SUCCESS, reason == null)
                put(AnalyticsParams.IS_CACHED, isCached)
                put(AnalyticsParams.IS_PRO, isPro)
                reason?.let { put(AnalyticsParams.REASON, it) }
            },
        )
    }

    private companion object {
        const val UNKNOWN_REASON = "unknown"
        const val PERF_LOG_TAG = "DayStudyPerf"
    }
}
