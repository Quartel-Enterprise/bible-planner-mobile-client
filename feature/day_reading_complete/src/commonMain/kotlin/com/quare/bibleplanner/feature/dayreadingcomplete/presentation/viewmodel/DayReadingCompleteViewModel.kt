package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_never_show_confirmation
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_offline_message
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.core.daystudy.domain.store.DayStudyQuotaPrefetchStore
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.model.route.toDayNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetScheduledDay
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.SetStudySuggestionEnabled
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.toPlanTypeAnalyticsValue
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ClassifyDayTimingUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ResolveStudyCtaStateUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DayReadingCompleteViewModel(
    private val route: DayReadingCompleteNavRoute,
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
    private val setStudySuggestionEnabled: SetStudySuggestionEnabled,
    private val navigator: Navigator,
    private val studyUnlockResultStore: StudyUnlockResultStore,
    trackEvent: TrackEvent,
) : TrackedViewModel<DayReadingCompleteUiEvent>(trackEvent) {
    private val readingPlanType = ReadingPlanType.valueOf(route.readingPlanType)
    private val dayLocation = PlanDayLocationModel(
        weekNumber = route.weekNumber,
        dayNumber = route.dayNumber,
        readingPlanType = readingPlanType,
    )
    val uiState: StateFlow<DayReadingCompleteUiState>
        field = MutableStateFlow<DayReadingCompleteUiState>(DayReadingCompleteUiState.Loading)

    val uiAction: SharedFlow<DayReadingCompleteUiAction>
        field = MutableSharedFlow<DayReadingCompleteUiAction>(extraBufferCapacity = 1)

    private var passages: List<PassageModel> = emptyList()
    private var hasTrackedShown = false
    private var rewardedReadingLabel = ""
    private val generationKey = generationCoordinator.keyOf(route.toDayNavRoute())

    init {
        loadDay()
        observeRewardedUnlock()
    }

    private fun observeRewardedUnlock() {
        viewModelScope.launch {
            studyUnlockResultStore.observeEarned(REWARDED_UNLOCK_REQUEST_PREFIX + generationKey).collect {
                startGeneration(
                    readingLabel = rewardedReadingLabel,
                    isRewarded = true,
                )
            }
        }
    }

    override fun handleEvent(event: DayReadingCompleteUiEvent) {
        when (event) {
            is DayReadingCompleteUiEvent.OnCtaClick -> onCtaClick(event.readingLabel)
            DayReadingCompleteUiEvent.OnDismiss -> navigator.navigateBack()
            DayReadingCompleteUiEvent.OnNeverShowAgainClick -> disableSuggestion()
        }
    }

    private fun disableSuggestion() {
        viewModelScope.launch {
            setStudySuggestionEnabled(false)
            uiAction.emit(
                DayReadingCompleteUiAction.ShowSnackBar(Res.string.day_reading_complete_never_show_confirmation),
            )
            navigator.navigateBack()
        }
    }

    /*
     * Why: the celebration shows as soon as the day is known; only the CTA waits for the network
     * quota, so the earned moment is never traded for a spinner.
     */
    private fun loadDay() {
        viewModelScope.launch {
            val day = getScheduledDay(
                weekNumber = route.weekNumber,
                dayNumber = route.dayNumber,
                readingPlanType = readingPlanType,
            )
            if (day == null) return@launch
            passages = day.passages
            val timing = classifyDayTiming(day.plannedReadDate)
            val chapterCount = day.passages.sumOf { it.chapters.size }
            val language = getAppLanguageFlow().first()
            uiState.update {
                DayReadingCompleteUiState.Loaded(
                    timing = timing,
                    plannedReadDate = day.plannedReadDate,
                    passages = day.passages,
                    chapterCount = chapterCount,
                    ctaState = Loadable.Loading,
                    language = language,
                )
            }
            observeIsProUser().collectLatest { isPro ->
                quotaPrefetchStore.findQuota(dayLocation)?.let { prefetchedQuota ->
                    showCta(
                        quota = prefetchedQuota,
                        isPro = isPro,
                        timing = timing,
                        chapterCount = chapterCount,
                    )
                }
                showCta(
                    quota = getDayStudyQuota(day.passages),
                    isPro = isPro,
                    timing = timing,
                    chapterCount = chapterCount,
                )
            }
        }
    }

    /*
     * Why: a prefetched quota is a head start, not the truth, so the fresh one always lands on top;
     * tracking fires once, on what the reader actually saw first.
     */
    private suspend fun showCta(
        quota: DayStudyQuotaModel,
        isPro: Boolean,
        timing: DayTimingState,
        chapterCount: Int,
    ) {
        val ctaState = resolveStudyCtaState(isPro, quota)
        uiState.update { state ->
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
            name = AnalyticsEventNames.DAY_READING_COMPLETE_SHOWN,
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to route.readingPlanType.toPlanTypeAnalyticsValue(),
                AnalyticsParams.WEEK_NUMBER to route.weekNumber,
                AnalyticsParams.DAY_NUMBER to route.dayNumber,
                AnalyticsParams.TIMING to timing.name.lowercase(),
                AnalyticsParams.ACCOUNT_STATE to ctaState.toAnalyticsValue(),
                AnalyticsParams.CHAPTER_COUNT to chapterCount,
            ),
        )
    }

    private fun onCtaClick(readingLabel: String) {
        val ctaState = (uiState.value as? DayReadingCompleteUiState.Loaded)
            ?.ctaState
            ?.valueOrNull()
            ?: return
        trackEvent(
            name = AnalyticsEventNames.DAY_READING_COMPLETE_CTA_CLICKED,
            params = mapOf(
                AnalyticsParams.ACCOUNT_STATE to ctaState.toAnalyticsValue(),
                AnalyticsParams.SOURCE to SOURCE_VALUE,
            ),
        )
        when (ctaState) {
            is StudyCtaState.FreeExhausted -> onExhaustedCtaClick(
                ctaState = ctaState,
                readingLabel = readingLabel,
            )

            is StudyCtaState.FreeWithQuota, StudyCtaState.Pro -> startGeneration(
                readingLabel = readingLabel,
                isRewarded = false,
            )
        }
    }

    private fun onExhaustedCtaClick(
        ctaState: StudyCtaState.FreeExhausted,
        readingLabel: String,
    ) {
        rewardedReadingLabel = readingLabel
        when {
            generationCoordinator.hasUnservedReward(generationKey) -> startGeneration(
                readingLabel = readingLabel,
                isRewarded = true,
            )

            ctaState.isRewardedUnlockOffered -> navigator.navigate(
                StudyUnlockNavRoute(
                    surface = StudyUnlockSurface.DAY_READING_COMPLETE,
                    paywallSource = PaywallEntrySource.DAY_STUDY,
                    requestKey = REWARDED_UNLOCK_REQUEST_PREFIX + generationKey,
                    rewardedRemainingToday = ctaState.rewardedRemainingToday,
                ),
            )

            else -> navigator.navigate(PaywallNavRoute(PaywallEntrySource.DAY_STUDY))
        }
    }

    private fun startGeneration(
        readingLabel: String,
        isRewarded: Boolean,
    ) {
        viewModelScope.launch {
            if (!isConnected()) {
                emitAction(
                    DayReadingCompleteUiAction.ShowSnackBar(Res.string.day_reading_complete_offline_message),
                )
                return@launch
            }
            if (observeAuthenticatedUserId().first() == null) {
                navigator.navigate(LoginWarningNavRoute(LoginWarningReason.DayStudy.key))
                return@launch
            }
            generationCoordinator.start(
                passages = passages,
                dayRoute = route.toDayNavRoute(),
                label = readingLabel,
                isRewarded = isRewarded,
            )
            navigator.navigateReplacingTop(
                DayStudyNavRoute(
                    dayNumber = route.dayNumber,
                    weekNumber = route.weekNumber,
                    readingPlanType = route.readingPlanType,
                ),
            )
        }
    }

    private fun emitAction(action: DayReadingCompleteUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }

    private companion object {
        const val SOURCE_VALUE = "day_reading_complete"
        const val REWARDED_UNLOCK_REQUEST_PREFIX = "day_reading_complete|"
    }
}
