package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_offline_message
import bibleplanner.feature.day_study.generated.resources.ai_study_wait_for_generations
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.toPlanTypeAnalyticsValue
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardLoader
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiState
import com.quare.bibleplanner.feature.daystudy.presentation.model.toPhaseIndex
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.quare.connectivity.IsConnected
import software.quare.loadable.Loadable
import software.quare.loadable.isLoading
import software.quare.loadable.toLoaded
import software.quare.loadable.valueOrNull

internal class DayStudyViewModel(
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val isConnected: IsConnected,
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val cardLoader: DayStudyCardLoader,
    private val navigator: Navigator,
    private val studyUnlockResultStore: StudyUnlockResultStore,
    trackEvent: TrackEvent,
) : TrackedViewModel<DayStudyUiEvent>(trackEvent) {
    val uiState: StateFlow<DayStudyUiState>
        field = MutableStateFlow<DayStudyUiState>(
            DayStudyUiState(
                card = Loadable.Loading,
                generation = null,
                isOpeningStudy = false,
            ),
        )

    val uiAction: SharedFlow<DayStudyUiAction>
        field = MutableSharedFlow<DayStudyUiAction>()

    private var passages: List<PassageModel> = emptyList()
    private var dayRoute: DayNavRoute? = null
    private var label: String = ""
    private var jobKey: String? = null
    private var observeCardJob: Job? = null
    private var observeJobJob: Job? = null
    private var observeRewardedUnlockJob: Job? = null

    override fun handleEvent(event: DayStudyUiEvent) {
        when (event) {
            is DayStudyUiEvent.OnStart -> onStart(event.passages, event.dayRoute, event.label)
            DayStudyUiEvent.OnCardClick -> onCardClick()
        }
    }

    private fun onStart(
        startPassages: List<PassageModel>,
        route: DayNavRoute,
        studyLabel: String,
    ) {
        val routeChanged = dayRoute != route
        passages = startPassages
        dayRoute = route
        label = studyLabel
        val key = generationCoordinator.keyOf(route)
        jobKey = key
        generationCoordinator.setActive(key)
        if (routeChanged) {
            cardLoader.startLoad(LOAD_TARGET)
            uiState.update {
                it.copy(
                    card = Loadable.Loading,
                    generation = null,
                    isOpeningStudy = false,
                )
            }
        }
        observeCard()
        observeJob()
        observeRewardedUnlock(key)
    }

    private fun observeRewardedUnlock(key: String) {
        observeRewardedUnlockJob?.cancel()
        observeRewardedUnlockJob = viewModelScope.launch {
            studyUnlockResultStore.observeEarned(buildRewardedUnlockRequestKey(key)).collect {
                withOpeningIndicator { startGeneration(isRewarded = true) }
            }
        }
    }

    private fun observeCard() {
        observeCardJob?.cancel()
        observeCardJob = viewModelScope.launch {
            cardLoader
                .observe(
                    passages = ::passages,
                    useCache = uiState.value.card.isLoading(),
                ).collect(::showCard)
        }
    }

    private fun showCard(card: DayStudyCardUiModel) {
        uiState.update { it.copy(card = card.toLoaded()) }
    }

    private fun observeJob() {
        val key = jobKey ?: return
        observeJobJob?.cancel()
        observeJobJob = viewModelScope.launch {
            generationCoordinator.jobs
                .map { jobs -> jobs.firstOrNull { it.key == key } }
                .distinctUntilChanged()
                .collect(::onJobUpdate)
        }
    }

    private suspend fun onJobUpdate(job: DayStudyGenerationJob?) {
        when (val status = job?.status) {
            null -> uiState.update { it.copy(generation = null) }

            DayStudyGenerationStatus.Generating -> uiState.update {
                it.copy(generation = DayStudyGenerationUiModel(job.phase?.toPhaseIndex() ?: 0))
            }

            is DayStudyGenerationStatus.Done -> onJobDone()

            is DayStudyGenerationStatus.Failed -> onJobFailed(
                isLimitReached = status.isLimitReached,
                isOffline = status.isOffline,
            )
        }
    }

    private suspend fun onJobDone() {
        jobKey ?: return
        cardLoader.reload(passages)?.let(::showCard)
        uiState.update { it.copy(generation = null) }
    }

    private suspend fun onJobFailed(
        isLimitReached: Boolean,
        isOffline: Boolean,
    ) {
        jobKey ?: return
        uiState.update { it.copy(generation = null) }
        if (isLimitReached) {
            lockCard()
        }
        if (isOffline) uiAction.emit(DayStudyUiAction.ShowSnackBar(Res.string.ai_study_offline_message))
    }

    private fun onCardClick() {
        val card = uiState.value.card.valueOrNull() ?: return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_CARD_CLICKED,
            params = mapOf(
                AnalyticsParams.CARD_MODE to card.mode
                    ?.name
                    ?.lowercase()
                    .orEmpty(),
                AnalyticsParams.IS_PRO to card.isPro,
                AnalyticsParams.SOURCE to CARD_CLICK_SOURCE,
            ),
        )
        if (uiState.value.generation != null) {
            emitAction(DayStudyUiAction.NavigateToStudy)
            return
        }
        when (card.mode) {
            DayStudyCardMode.LOCKED -> onLockedCardClick(card)
            null, DayStudyCardMode.GENERATE -> generateIfLoggedIn()
            DayStudyCardMode.VIEW -> emitAction(DayStudyUiAction.NavigateToStudy)
        }
    }

    private fun onLockedCardClick(card: DayStudyCardUiModel) {
        val key = jobKey
        when {
            key == null -> emitAction(DayStudyUiAction.NavigateToPaywall)

            generationCoordinator.hasUnservedReward(key) -> viewModelScope.launch {
                withOpeningIndicator { startGeneration(isRewarded = true) }
            }

            card.isRewardedUnlockOffered -> navigator.navigate(
                StudyUnlockNavRoute(
                    surface = StudyUnlockSurface.DAY_STUDY,
                    paywallSource = PaywallEntrySource.DAY_STUDY,
                    requestKey = buildRewardedUnlockRequestKey(key),
                    rewardedRemainingToday = card.rewardedRemainingToday,
                    canUnlockWithoutVideo = false,
                ),
            )

            else -> emitAction(DayStudyUiAction.NavigateToPaywall)
        }
    }

    private fun generateIfLoggedIn() {
        viewModelScope.launch {
            withOpeningIndicator {
                if (observeAuthenticatedUserId().first() == null) {
                    uiAction.emit(DayStudyUiAction.NavigateToLoginWarning)
                } else {
                    startGeneration(isRewarded = false)
                }
            }
        }
    }

    private suspend fun withOpeningIndicator(block: suspend () -> Unit) {
        uiState.update { it.copy(isOpeningStudy = true) }
        try {
            block()
        } finally {
            uiState.update { it.copy(isOpeningStudy = false) }
        }
    }

    private suspend fun startGeneration(isRewarded: Boolean) {
        val route = dayRoute ?: return
        if (!isConnected()) {
            trackEvent(
                name = AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED,
                params = getDayParams(route) + mapOf(
                    AnalyticsParams.REASON to OFFLINE_REASON,
                    AnalyticsParams.IS_PRO to cardLoader.isPro,
                ),
            )
            uiAction.emit(DayStudyUiAction.ShowSnackBar(Res.string.ai_study_offline_message))
            return
        }
        val quota = getDayStudyQuota(passages)
        if (!isRewarded && !canStartFreeGeneration(quota)) return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_GENERATION_STARTED,
            params = getDayParams(route) + mapOf(
                AnalyticsParams.IS_PRO to cardLoader.isPro,
                AnalyticsParams.REMAINING_FREE to quota.remainingFree,
                AnalyticsParams.IS_REWARDED to isRewarded,
            ),
        )
        jobKey = generationCoordinator.start(
            passages = passages,
            dayRoute = route,
            label = label,
            isRewarded = isRewarded,
        )
        uiState.update { it.copy(generation = DayStudyGenerationUiModel(currentPhaseIndex = 0)) }
        uiAction.emit(DayStudyUiAction.NavigateToStudy)
    }

    private suspend fun canStartFreeGeneration(quota: DayStudyQuotaModel): Boolean {
        if (cardLoader.isPro || quota.isUnlockedForDay) return true
        val inFlight = generationCoordinator.getGeneratingCount(excludingKey = jobKey)
        if (inFlight < quota.remainingFree) return true
        uiAction.emit(
            DayStudyUiAction.ShowSnackBarPlural(
                resource = Res.plurals.ai_study_wait_for_generations,
                count = inFlight,
            ),
        )
        return false
    }

    private suspend fun lockCard() {
        val card = uiState.value.card.valueOrNull() ?: return
        val lockedCard = cardLoader.loadLocked(
            card = card,
            passages = passages,
        )
        uiState.update { it.copy(card = lockedCard.toLoaded()) }
    }

    private fun buildRewardedUnlockRequestKey(key: String): String = REWARDED_UNLOCK_REQUEST_PREFIX + key

    private fun getDayParams(route: DayNavRoute): Map<String, Any> = mapOf(
        AnalyticsParams.PLAN_TYPE to route.readingPlanType.toPlanTypeAnalyticsValue(),
        AnalyticsParams.WEEK_NUMBER to route.weekNumber,
        AnalyticsParams.DAY_NUMBER to route.dayNumber,
    )

    private fun emitAction(action: DayStudyUiAction) {
        viewModelScope.launch {
            uiAction.emit(action)
        }
    }

    override fun onCleared() {
        super.onCleared()
        jobKey?.let(generationCoordinator::clearActive)
    }

    private companion object {
        const val OFFLINE_REASON = "offline"
        const val LOAD_TARGET = "card"
        const val CARD_CLICK_SOURCE = "day_screen"
        const val REWARDED_UNLOCK_REQUEST_PREFIX = "day_study_card|"
    }
}
