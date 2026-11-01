package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_never_show_confirmation
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_offline_message
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.toDayNavRoute
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.SetStudySuggestionEnabled
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.factory.DayReadingCompleteCtaResolver
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.factory.DayReadingCompleteStateLoader
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteCtaDecision
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import software.quare.connectivity.IsConnected

internal class DayReadingCompleteViewModel(
    private val route: DayReadingCompleteNavRoute,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val isConnected: IsConnected,
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val stateLoader: DayReadingCompleteStateLoader,
    private val ctaResolver: DayReadingCompleteCtaResolver,
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

    private fun loadDay() {
        viewModelScope.launch {
            stateLoader
                .observe(
                    day = dayLocation,
                    shownEventName = AnalyticsEventNames.DAY_READING_COMPLETE_SHOWN,
                ).collect { loaded ->
                    passages = loaded.passages
                    uiState.value = loaded
                }
        }
    }

    private fun onCtaClick(readingLabel: String) {
        val decision = ctaResolver.resolveClick(
            state = uiState.value,
            clickedEventName = AnalyticsEventNames.DAY_READING_COMPLETE_CTA_CLICKED,
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

            DayReadingCompleteCtaDecision.OpenPaywall -> navigator.navigate(
                PaywallNavRoute(PaywallEntrySource.DAY_STUDY),
            )
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
