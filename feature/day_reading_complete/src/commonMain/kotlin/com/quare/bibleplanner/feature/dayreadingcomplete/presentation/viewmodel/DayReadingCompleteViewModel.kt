package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_offline_message
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.toDayNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetDay
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
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.DayStudyGenerationCoordinator
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

class DayReadingCompleteViewModel(
    private val route: DayReadingCompleteNavRoute,
    private val getDay: GetDay,
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val observeIsProUser: ObserveIsProUser,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val isConnected: IsConnected,
    private val classifyDayTiming: ClassifyDayTimingUseCase,
    private val resolveStudyCtaState: ResolveStudyCtaStateUseCase,
    private val generationCoordinator: DayStudyGenerationCoordinator,
    trackEvent: TrackEvent,
) : TrackedViewModel<DayReadingCompleteUiEvent>(trackEvent) {
    private val readingPlanType = ReadingPlanType.valueOf(route.readingPlanType)
    private val _uiState = MutableStateFlow<DayReadingCompleteUiState>(DayReadingCompleteUiState.Loading)
    val uiState: StateFlow<DayReadingCompleteUiState> = _uiState.asStateFlow()

    private val _uiAction = MutableSharedFlow<DayReadingCompleteUiAction>(extraBufferCapacity = 1)
    val uiAction: SharedFlow<DayReadingCompleteUiAction> = _uiAction

    private var passages: List<PassageModel> = emptyList()
    private var hasTrackedShown = false

    init {
        loadDay()
    }

    override fun handleEvent(event: DayReadingCompleteUiEvent) {
        when (event) {
            is DayReadingCompleteUiEvent.OnCtaClick -> onCtaClick(event.readingLabel)
            DayReadingCompleteUiEvent.OnDismiss -> emitAction(DayReadingCompleteUiAction.NavigateBack)
        }
    }

    private fun loadDay() {
        viewModelScope.launch {
            val day = getDay(
                weekNumber = route.weekNumber,
                dayNumber = route.dayNumber,
                readingPlanType = readingPlanType,
            )
            if (day == null) return@launch
            passages = day.passages
            val timing = classifyDayTiming(day.plannedReadDate)
            val chapterCount = day.passages.sumOf { it.chapters.size }
            val language = getAppLanguageFlow().first()
            observeIsProUser().collectLatest { isPro ->
                val quota = getDayStudyQuota(day.passages)
                val ctaState = resolveStudyCtaState(isPro, quota)
                _uiState.update {
                    DayReadingCompleteUiState.Loaded(
                        timing = timing,
                        plannedReadDate = day.plannedReadDate,
                        passages = day.passages,
                        chapterCount = chapterCount,
                        ctaState = ctaState,
                        language = language,
                    )
                }
                trackShownOnce(timing, ctaState)
            }
        }
    }

    private fun trackShownOnce(
        timing: DayTimingState,
        ctaState: StudyCtaState,
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
            ),
        )
    }

    private fun onCtaClick(readingLabel: String) {
        val state = _uiState.value as? DayReadingCompleteUiState.Loaded ?: return
        trackEvent(
            name = AnalyticsEventNames.DAY_READING_COMPLETE_CTA_CLICKED,
            params = mapOf(
                AnalyticsParams.ACCOUNT_STATE to state.ctaState.toAnalyticsValue(),
                AnalyticsParams.SOURCE to SOURCE_VALUE,
            ),
        )
        when (state.ctaState) {
            is StudyCtaState.FreeExhausted -> emitAction(
                DayReadingCompleteUiAction.NavigateToRoute(
                    route = PaywallNavRoute(PaywallEntrySource.DAY_STUDY),
                    replace = false,
                ),
            )

            is StudyCtaState.FreeWithQuota, StudyCtaState.Pro -> startGeneration(readingLabel)
        }
    }

    private fun startGeneration(readingLabel: String) {
        viewModelScope.launch {
            if (!isConnected()) {
                emitAction(
                    DayReadingCompleteUiAction.ShowSnackBar(Res.string.day_reading_complete_offline_message),
                )
                return@launch
            }
            if (observeAuthenticatedUserId().first() == null) {
                emitAction(
                    DayReadingCompleteUiAction.NavigateToRoute(
                        route = LoginWarningNavRoute(LoginWarningReason.DayStudy.key),
                        replace = false,
                    ),
                )
                return@launch
            }
            generationCoordinator.start(passages, route.toDayNavRoute(), readingLabel)
            emitAction(
                DayReadingCompleteUiAction.NavigateToRoute(
                    route = DayStudyNavRoute(
                        dayNumber = route.dayNumber,
                        weekNumber = route.weekNumber,
                        readingPlanType = route.readingPlanType,
                    ),
                    replace = true,
                ),
            )
        }
    }

    private fun emitAction(action: DayReadingCompleteUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }

    private companion object {
        const val SOURCE_VALUE = "day_reading_complete"
    }
}

private fun StudyCtaState.toAnalyticsValue(): String = when (this) {
    is StudyCtaState.FreeWithQuota -> "free"
    is StudyCtaState.FreeExhausted -> "free_exhausted"
    StudyCtaState.Pro -> "pro"
}
