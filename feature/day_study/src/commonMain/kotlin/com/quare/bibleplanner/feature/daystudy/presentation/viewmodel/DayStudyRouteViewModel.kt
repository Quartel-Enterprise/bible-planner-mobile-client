package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_limit_reached_message
import bibleplanner.feature.day_study.generated.resources.ai_study_wait_for_generations
import com.quare.bibleplanner.core.books.util.getReadingLabel
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayPassagesForDayStudyUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.ChatEntrySource
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.model.route.toDayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.toPlanTypeAnalyticsValue
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardLoader
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationError
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationPhase
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiState
import com.quare.bibleplanner.feature.daystudy.presentation.model.toPhaseIndex
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.quare.connectivity.IsConnected
import software.quare.loadable.Loadable
import software.quare.loadable.isLoading
import software.quare.loadable.toLoaded
import software.quare.loadable.valueOrNull
import kotlin.time.Duration.Companion.milliseconds

internal class DayStudyRouteViewModel(
    private val getDayPassages: GetDayPassagesForDayStudyUseCase,
    private val getDayStudy: GetDayStudyUseCase,
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val hasCachedStudy: HasCachedStudyUseCase,
    private val isConnected: IsConnected,
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val cardLoader: DayStudyCardLoader,
    private val navigator: Navigator,
    private val studyUnlockResultStore: StudyUnlockResultStore,
    route: DayStudyNavRoute,
    platform: Platform,
    trackEvent: TrackEvent,
) : TrackedViewModel<DayStudyRouteUiEvent>(trackEvent) {
    val uiState: StateFlow<DayStudyRouteUiState>
        field = MutableStateFlow<DayStudyRouteUiState>(
            DayStudyRouteUiState(
                card = Loadable.Loading,
                generation = null,
                generationError = null,
                openStudy = null,
                isOpeningStudy = false,
                passageLabel = null,
                platform = platform,
            ),
        )

    val uiAction: SharedFlow<DayStudyRouteUiAction>
        field = MutableSharedFlow<DayStudyRouteUiAction>()

    private val completionPause = 700.milliseconds

    private val dayRoute: DayNavRoute = route.toDayNavRoute()
    private val jobKey: String = generationCoordinator.keyOf(dayRoute)
    private var passages: List<PassageModel> = emptyList()
    private var label: String = ""
    private var isStarted = false

    init {
        cardLoader.startLoad(LOAD_TARGET)
        generationCoordinator.setActive(jobKey)
        observePassages()
        observeRewardedUnlock()
    }

    override fun handleEvent(event: DayStudyRouteUiEvent) {
        when (event) {
            DayStudyRouteUiEvent.OnCardClick -> onCardClick()
            DayStudyRouteUiEvent.OnRetryClick -> onRetryClick()
            DayStudyRouteUiEvent.OnAskAiClick -> onAskAiClick()
        }
    }

    private fun onAskAiClick() {
        trackEvent(
            name = AnalyticsEventNames.AI_CHAT_ENTRY_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to ChatEntrySource.DAY_STUDY_QUESTIONS.key),
        )
        navigator.navigate(
            ChatNavRoute(
                source = ChatEntrySource.DAY_STUDY_QUESTIONS,
                dayNumber = dayRoute.dayNumber,
                weekNumber = dayRoute.weekNumber,
                readingPlanType = dayRoute.readingPlanType,
                bookId = null,
                chapterNumber = null,
            ),
        )
    }

    private fun onRetryClick() {
        uiState.update { it.copy(generationError = null) }
        viewModelScope.launch {
            withOpeningIndicator {
                startGenerationOrCachedOpen(isRewarded = generationCoordinator.hasUnservedReward(jobKey))
            }
        }
    }

    private fun observeRewardedUnlock() {
        viewModelScope.launch {
            studyUnlockResultStore.observeEarned(REWARDED_UNLOCK_REQUEST_PREFIX + jobKey).collect {
                withOpeningIndicator { startGenerationOrCachedOpen(isRewarded = true) }
            }
        }
    }

    private fun observePassages() {
        getDayPassages(
            weekNumber = dayRoute.weekNumber,
            dayNumber = dayRoute.dayNumber,
            readingPlanType = ReadingPlanType.valueOf(dayRoute.readingPlanType),
        ).filterNotNull()
            .onEach(::onPassagesLoaded)
            .launchIn(viewModelScope)
    }

    private suspend fun onPassagesLoaded(loaded: List<PassageModel>) {
        passages = loaded
        label = loaded.getReadingLabel()
        uiState.update { it.copy(passageLabel = label) }
        if (isStarted) return
        isStarted = true
        observeCard()
        observeJob()
    }

    private fun observeCard() {
        viewModelScope.launch {
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
        generationCoordinator.jobs
            .map { jobs -> jobs.firstOrNull { it.key == jobKey } }
            .distinctUntilChanged()
            .onEach(::onJobUpdate)
            .launchIn(viewModelScope)
    }

    private suspend fun onJobUpdate(job: DayStudyGenerationJob?) {
        when (val status = job?.status) {
            null -> Unit

            DayStudyGenerationStatus.Generating -> uiState.update {
                it.copy(
                    generation = DayStudyGenerationUiModel(job.phase?.toPhaseIndex() ?: 0),
                    generationError = null,
                )
            }

            is DayStudyGenerationStatus.Done -> onJobDone(status.study)

            is DayStudyGenerationStatus.Failed -> onJobFailed(
                isLimitReached = status.isLimitReached,
                isOffline = status.isOffline,
            )
        }
    }

    private suspend fun onJobDone(study: DayStudyModel) {
        if (uiState.value.generation != null) completeGenerationPhases()
        uiState.update {
            it.copy(
                generation = null,
                openStudy = study,
            )
        }
        trackStudyOpened(isCached = false)
        cardLoader.reload(passages)?.let(::showCard)
        generationCoordinator.acknowledge(jobKey)
    }

    private suspend fun onJobFailed(
        isLimitReached: Boolean,
        isOffline: Boolean,
    ) {
        val error = when {
            isLimitReached -> null
            isOffline -> DayStudyGenerationError.OFFLINE
            else -> DayStudyGenerationError.GENERIC
        }
        uiState.update {
            it.copy(
                generation = null,
                generationError = error,
            )
        }
        if (isLimitReached) {
            lockCard()
            uiAction.emit(DayStudyRouteUiAction.ShowSnackBar(Res.string.ai_study_limit_reached_message))
        }
        generationCoordinator.acknowledge(jobKey)
    }

    private fun onCardClick() {
        val card = uiState.value.card.valueOrNull() ?: return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_CARD_CLICKED,
            params = buildMap {
                card.mode?.let { put(AnalyticsParams.CARD_MODE, it.name.lowercase()) }
                put(AnalyticsParams.IS_PRO, card.isPro)
                put(AnalyticsParams.SOURCE, CARD_CLICK_SOURCE)
            },
        )
        if (uiState.value.openStudy != null || uiState.value.generation != null) return
        when (card.mode) {
            DayStudyCardMode.LOCKED -> onLockedCardClick(card)
            null, DayStudyCardMode.GENERATE -> generateIfLoggedIn()
            DayStudyCardMode.VIEW -> generateOrOpen()
        }
    }

    private fun onLockedCardClick(card: DayStudyCardUiModel) {
        when {
            generationCoordinator.hasUnservedReward(jobKey) -> viewModelScope.launch {
                withOpeningIndicator { startGenerationOrCachedOpen(isRewarded = true) }
            }

            card.isRewardedUnlockOffered -> navigator.navigate(
                StudyUnlockNavRoute(
                    surface = StudyUnlockSurface.DAY_STUDY,
                    paywallSource = PaywallEntrySource.DAY_STUDY_DETAIL,
                    requestKey = REWARDED_UNLOCK_REQUEST_PREFIX + jobKey,
                    rewardedRemainingToday = card.rewardedRemainingToday,
                    canUnlockWithoutVideo = false,
                ),
            )

            else -> navigator.navigate(PaywallNavRoute(PaywallEntrySource.DAY_STUDY_DETAIL))
        }
    }

    private fun generateIfLoggedIn() {
        viewModelScope.launch {
            withOpeningIndicator {
                if (observeAuthenticatedUserId().first() == null) {
                    navigator.navigate(LoginWarningNavRoute(LoginWarningReason.DayStudy.key))
                } else {
                    startGenerationOrCachedOpen(isRewarded = false)
                }
            }
        }
    }

    private fun generateOrOpen() {
        viewModelScope.launch { withOpeningIndicator { startGenerationOrCachedOpen(isRewarded = false) } }
    }

    private suspend fun withOpeningIndicator(block: suspend () -> Unit) {
        uiState.update { it.copy(isOpeningStudy = true) }
        try {
            block()
        } finally {
            uiState.update { it.copy(isOpeningStudy = false) }
        }
    }

    private suspend fun startGenerationOrCachedOpen(isRewarded: Boolean) {
        if (hasCachedStudy(passages)) {
            openCachedStudy()
            return
        }
        if (!isConnected()) {
            trackEvent(
                name = AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED,
                params = getDayParams() + mapOf(
                    AnalyticsParams.REASON to OFFLINE_REASON,
                    AnalyticsParams.IS_PRO to cardLoader.isPro,
                ),
            )
            uiState.update { it.copy(generationError = DayStudyGenerationError.OFFLINE) }
            return
        }
        val quota = getDayStudyQuota(passages)
        if (!isRewarded && !canStartFreeGeneration(quota)) return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_GENERATION_STARTED,
            params = getDayParams() + mapOf(
                AnalyticsParams.IS_PRO to cardLoader.isPro,
                AnalyticsParams.REMAINING_FREE to quota.remainingFree,
                AnalyticsParams.IS_REWARDED to isRewarded,
            ),
        )
        generationCoordinator.start(
            passages = passages,
            dayRoute = dayRoute,
            label = label,
            isRewarded = isRewarded,
        )
        uiState.update { it.copy(generation = DayStudyGenerationUiModel(currentPhaseIndex = 0)) }
    }

    private suspend fun canStartFreeGeneration(quota: DayStudyQuotaModel): Boolean {
        if (cardLoader.isPro || quota.isUnlockedForDay) return true
        val inFlight = generationCoordinator.getGeneratingCount(excludingKey = jobKey)
        if (inFlight < quota.remainingFree) return true
        uiAction.emit(
            DayStudyRouteUiAction.ShowSnackBarPlural(
                resource = Res.plurals.ai_study_wait_for_generations,
                count = inFlight,
            ),
        )
        return false
    }

    private suspend fun openCachedStudy() {
        val study = getDayStudy(
            passages = passages,
            isRewarded = false,
        ).mapNotNull { (it as? DayStudyGenerationEventModel.Completed)?.study }
            .first()
        uiState.update { it.copy(openStudy = study) }
        trackStudyOpened(isCached = true)
    }

    private suspend fun completeGenerationPhases() {
        uiState.update { state ->
            state.copy(
                generation = state.generation?.copy(
                    currentPhaseIndex = DayStudyGenerationPhase.entries.size,
                ),
            )
        }
        delay(completionPause)
    }

    private suspend fun lockCard() {
        val card = uiState.value.card.valueOrNull() ?: return
        val lockedCard = cardLoader.loadLocked(
            card = card,
            passages = passages,
        )
        uiState.update { it.copy(card = lockedCard.toLoaded()) }
    }

    private fun trackStudyOpened(isCached: Boolean) {
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_OPENED,
            params = mapOf(AnalyticsParams.IS_CACHED to isCached),
        )
    }

    private fun getDayParams(): Map<String, Any> = mapOf(
        AnalyticsParams.PLAN_TYPE to dayRoute.readingPlanType.toPlanTypeAnalyticsValue(),
        AnalyticsParams.WEEK_NUMBER to dayRoute.weekNumber,
        AnalyticsParams.DAY_NUMBER to dayRoute.dayNumber,
    )

    private fun emitAction(action: DayStudyRouteUiAction) {
        viewModelScope.launch {
            uiAction.emit(action)
        }
    }

    override fun onCleared() {
        super.onCleared()
        generationCoordinator.clearActive(jobKey)
    }

    private companion object {
        const val OFFLINE_REASON = "offline"
        const val LOAD_TARGET = "panel"
        const val CARD_CLICK_SOURCE = "day_study_detail"
        const val REWARDED_UNLOCK_REQUEST_PREFIX = "day_study_detail|"
    }
}
