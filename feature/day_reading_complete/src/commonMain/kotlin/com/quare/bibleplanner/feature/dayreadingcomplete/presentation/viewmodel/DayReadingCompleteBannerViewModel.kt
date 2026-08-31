package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_offline_message
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.toDayStudyNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetScheduledDay
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.toPlanTypeAnalyticsValue
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ClassifyDayTimingUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ResolveStudyCtaStateUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteBannerUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteBannerUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.daystudy.domain.store.DayStudyQuotaPrefetchStore
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DayReadingCompleteBannerViewModel(
    private val day: PlanDayLocationModel,
    private val getScheduledDay: GetScheduledDay,
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val observeIsProUser: ObserveIsProUser,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val isConnected: IsConnected,
    private val classifyDayTiming: ClassifyDayTimingUseCase,
    private val resolveStudyCtaState: ResolveStudyCtaStateUseCase,
    private val quotaPrefetchStore: DayStudyQuotaPrefetchStore,
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<DayReadingCompleteBannerUiEvent>(trackEvent) {
    private val _uiState = MutableStateFlow<DayReadingCompleteUiState>(DayReadingCompleteUiState.Loading)
    val uiState: StateFlow<DayReadingCompleteUiState> = _uiState.asStateFlow()

    private val _uiAction = MutableSharedFlow<DayReadingCompleteBannerUiAction>(extraBufferCapacity = 1)
    val uiAction: SharedFlow<DayReadingCompleteBannerUiAction> = _uiAction

    private var passages: List<PassageModel> = emptyList()
    private var hasTrackedShown = false

    init {
        loadDay()
    }

    override fun handleEvent(event: DayReadingCompleteBannerUiEvent) {
        when (event) {
            is DayReadingCompleteBannerUiEvent.OnCtaClick -> onCtaClick(event.readingLabel)
            DayReadingCompleteBannerUiEvent.OnDismissClick -> emitAction(DayReadingCompleteBannerUiAction.Dismiss)
        }
    }

    private fun loadDay() {
        viewModelScope.launch {
            val scheduledDay = getScheduledDay(
                weekNumber = day.weekNumber,
                dayNumber = day.dayNumber,
                readingPlanType = day.readingPlanType,
            )
            if (scheduledDay == null) return@launch
            passages = scheduledDay.passages
            val timing = classifyDayTiming(scheduledDay.plannedReadDate)
            val chapterCount = scheduledDay.passages.sumOf { it.chapters.size }
            val language = getAppLanguageFlow().first()
            _uiState.update {
                DayReadingCompleteUiState.Loaded(
                    timing = timing,
                    plannedReadDate = scheduledDay.plannedReadDate,
                    passages = scheduledDay.passages,
                    chapterCount = chapterCount,
                    ctaState = Loadable.Loading,
                    language = language,
                )
            }
            observeIsProUser().collectLatest { isPro ->
                quotaPrefetchStore.findQuota(day)?.let { prefetchedQuota ->
                    showCta(
                        quota = prefetchedQuota,
                        isPro = isPro,
                        timing = timing,
                        chapterCount = chapterCount,
                    )
                }
                showCta(
                    quota = getDayStudyQuota(scheduledDay.passages),
                    isPro = isPro,
                    timing = timing,
                    chapterCount = chapterCount,
                )
            }
        }
    }

    private fun showCta(
        quota: DayStudyQuotaModel,
        isPro: Boolean,
        timing: DayTimingState,
        chapterCount: Int,
    ) {
        val ctaState = resolveStudyCtaState(isPro, quota)
        _uiState.update { state ->
            (state as? DayReadingCompleteUiState.Loaded)
                ?.copy(ctaState = Loadable.Loaded(ctaState))
                ?: state
        }
        trackShownOnce(
            timing = timing,
            ctaState = ctaState,
            chapterCount = chapterCount,
        )
    }

    private fun trackShownOnce(
        timing: DayTimingState,
        ctaState: StudyCtaState,
        chapterCount: Int,
    ) {
        if (hasTrackedShown) return
        hasTrackedShown = true
        trackEvent(
            name = AnalyticsEventNames.DAY_READING_COMPLETE_BANNER_SHOWN,
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to day.readingPlanType.name.toPlanTypeAnalyticsValue(),
                AnalyticsParams.WEEK_NUMBER to day.weekNumber,
                AnalyticsParams.DAY_NUMBER to day.dayNumber,
                AnalyticsParams.TIMING to timing.name.lowercase(),
                AnalyticsParams.ACCOUNT_STATE to ctaState.toAnalyticsValue(),
                AnalyticsParams.CHAPTER_COUNT to chapterCount,
            ),
        )
    }

    private fun onCtaClick(readingLabel: String) {
        val ctaState = (_uiState.value as? DayReadingCompleteUiState.Loaded)
            ?.ctaState
            ?.valueOrNull()
            ?: return
        trackEvent(
            name = AnalyticsEventNames.DAY_READING_COMPLETE_BANNER_CTA_CLICKED,
            params = mapOf(
                AnalyticsParams.ACCOUNT_STATE to ctaState.toAnalyticsValue(),
                AnalyticsParams.SOURCE to SOURCE_VALUE,
            ),
        )
        when (ctaState) {
            is StudyCtaState.FreeExhausted -> {
                navigator.navigate(PaywallNavRoute(PaywallEntrySource.DAY_STUDY))
                emitAction(DayReadingCompleteBannerUiAction.Dismiss)
            }

            is StudyCtaState.FreeWithQuota, StudyCtaState.Pro -> startGeneration(readingLabel)
        }
    }

    private fun startGeneration(readingLabel: String) {
        viewModelScope.launch {
            if (!isConnected()) {
                emitAction(
                    DayReadingCompleteBannerUiAction.ShowSnackBar(Res.string.day_reading_complete_offline_message),
                )
                return@launch
            }
            if (observeAuthenticatedUserId().first() == null) {
                navigator.navigate(LoginWarningNavRoute(LoginWarningReason.DayStudy.key))
                return@launch
            }
            val dayRoute = day.toDayNavRoute()
            generationCoordinator.start(
                passages = passages,
                dayRoute = dayRoute,
                label = readingLabel,
            )
            navigator.navigate(dayRoute.toDayStudyNavRoute())
            emitAction(DayReadingCompleteBannerUiAction.Dismiss)
        }
    }

    private fun PlanDayLocationModel.toDayNavRoute(): DayNavRoute = DayNavRoute(
        dayNumber = dayNumber,
        weekNumber = weekNumber,
        readingPlanType = readingPlanType.name,
    )

    private fun emitAction(action: DayReadingCompleteBannerUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }

    private companion object {
        const val SOURCE_VALUE = "day_reading_complete_banner"
    }
}
