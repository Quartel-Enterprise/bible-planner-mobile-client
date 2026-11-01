package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_offline_message
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.toDayStudyNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.factory.DayReadingCompleteCtaResolver
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.factory.DayReadingCompleteStateLoader
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteBannerUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteBannerUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteCtaDecision
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import software.quare.connectivity.IsConnected

internal class DayReadingCompleteBannerViewModel(
    private val day: PlanDayLocationModel,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val isConnected: IsConnected,
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val stateLoader: DayReadingCompleteStateLoader,
    private val ctaResolver: DayReadingCompleteCtaResolver,
    private val navigator: Navigator,
    private val studyUnlockResultStore: StudyUnlockResultStore,
    trackEvent: TrackEvent,
) : TrackedViewModel<DayReadingCompleteBannerUiEvent>(trackEvent) {
    val uiState: StateFlow<DayReadingCompleteUiState>
        field = MutableStateFlow<DayReadingCompleteUiState>(DayReadingCompleteUiState.Loading)

    val uiAction: SharedFlow<DayReadingCompleteBannerUiAction>
        field = MutableSharedFlow<DayReadingCompleteBannerUiAction>(extraBufferCapacity = 1)

    private var passages: List<PassageModel> = emptyList()
    private var rewardedReadingLabel = ""
    private val generationKey = generationCoordinator.keyOf(day.toDayNavRoute())

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

    override fun handleEvent(event: DayReadingCompleteBannerUiEvent) {
        when (event) {
            is DayReadingCompleteBannerUiEvent.OnCtaClick -> onCtaClick(event.readingLabel)
            DayReadingCompleteBannerUiEvent.OnDismissClick -> emitAction(DayReadingCompleteBannerUiAction.Dismiss)
        }
    }

    private fun loadDay() {
        viewModelScope.launch {
            stateLoader
                .observe(
                    day = day,
                    shownEventName = AnalyticsEventNames.DAY_READING_COMPLETE_BANNER_SHOWN,
                ).collect { loaded ->
                    passages = loaded.passages
                    uiState.value = loaded
                }
        }
    }

    private fun onCtaClick(readingLabel: String) {
        val decision = ctaResolver.resolveClick(
            state = uiState.value,
            clickedEventName = AnalyticsEventNames.DAY_READING_COMPLETE_BANNER_CTA_CLICKED,
            source = SOURCE_VALUE,
            generationKey = generationKey,
            requestKey = REWARDED_UNLOCK_REQUEST_PREFIX + generationKey,
        ) ?: return
        rewardedReadingLabel = readingLabel
        when (decision) {
            is DayReadingCompleteCtaDecision.Generate -> startGeneration(
                readingLabel = readingLabel,
                isRewarded = decision.isRewarded,
            )

            is DayReadingCompleteCtaDecision.OfferRewardedUnlock -> navigator.navigate(decision.route)

            DayReadingCompleteCtaDecision.OpenPaywall -> {
                navigator.navigate(PaywallNavRoute(PaywallEntrySource.DAY_STUDY))
                emitAction(DayReadingCompleteBannerUiAction.Dismiss)
            }
        }
    }

    private fun startGeneration(
        readingLabel: String,
        isRewarded: Boolean,
    ) {
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
                isRewarded = isRewarded,
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
        viewModelScope.launch { uiAction.emit(action) }
    }

    private companion object {
        const val SOURCE_VALUE = "day_reading_complete_banner"
        const val REWARDED_UNLOCK_REQUEST_PREFIX = "day_reading_complete_banner|"
    }
}
