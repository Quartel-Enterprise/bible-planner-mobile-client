package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.factory

import com.quare.bibleplanner.core.daystudy.domain.store.DayStudyQuotaPrefetchStore
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetScheduledDay
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ClassifyDayTimingUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ResolveStudyCtaStateUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import software.quare.loadable.Loadable
import software.quare.loadable.toLoaded

internal class DayReadingCompleteStateLoader(
    private val getScheduledDay: GetScheduledDay,
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val observeIsProUser: ObserveIsProUser,
    private val classifyDayTiming: ClassifyDayTimingUseCase,
    private val resolveStudyCtaState: ResolveStudyCtaStateUseCase,
    private val quotaPrefetchStore: DayStudyQuotaPrefetchStore,
    private val trackEvent: TrackEvent,
) {
    /*
     * Why: the celebration shows as soon as the day is known; only the CTA waits for the network
     * quota, so the earned moment is never traded for a spinner. A prefetched quota is a head start,
     * not the truth, so the fresh one always lands on top. Shown is tracked once, on the CTA the reader saw first.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(
        day: PlanDayLocationModel,
        shownEventName: String,
    ): Flow<DayReadingCompleteUiState.Loaded> = flow {
        val scheduledDay = getScheduledDay(
            weekNumber = day.weekNumber,
            dayNumber = day.dayNumber,
            readingPlanType = day.readingPlanType,
        ) ?: return@flow
        val loaded = DayReadingCompleteUiState.Loaded(
            timing = classifyDayTiming(scheduledDay.plannedReadDate),
            plannedReadDate = scheduledDay.plannedReadDate,
            passages = scheduledDay.passages,
            chapterCount = scheduledDay.passages.sumOf { it.chapters.size },
            ctaState = Loadable.Loading,
            language = getAppLanguageFlow().first(),
        )
        emit(loaded)
        var hasTrackedShown = false
        emitAll(
            observeIsProUser().transformLatest { isPro ->
                flow {
                    quotaPrefetchStore.findQuota(day)?.let { emit(it) }
                    emit(getDayStudyQuota(scheduledDay.passages))
                }.collect { quota ->
                    val ctaState = resolveStudyCtaState(isPro, quota)
                    emit(loaded.copy(ctaState = ctaState.toLoaded()))
                    if (!hasTrackedShown) {
                        hasTrackedShown = true
                        trackEvent(
                            name = shownEventName,
                            params = loaded.toShownParams(
                                day = day,
                                ctaState = ctaState,
                            ),
                        )
                    }
                }
            },
        )
    }
}
