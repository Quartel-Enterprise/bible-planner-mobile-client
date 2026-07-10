package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_error
import bibleplanner.feature.day_study.generated.resources.ai_study_limit_reached_message
import bibleplanner.feature.day_study.generated.resources.ai_study_offline_message
import bibleplanner.feature.day_study.generated.resources.ai_study_wait_for_generations
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardUiModelFactory
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationPhase
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class DayStudyViewModel(
    private val getDayStudy: GetDayStudyUseCase,
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val hasCachedStudy: HasCachedStudyUseCase,
    private val isConnected: IsConnected,
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val observeIsProUser: ObserveIsProUser,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val cardUiModelFactory: DayStudyCardUiModelFactory,
    private val trackEvent: TrackEvent,
) : ViewModel() {
    private val _uiState: MutableStateFlow<DayStudyUiState> = MutableStateFlow(
        DayStudyUiState(
            card = Loadable.Loading,
            generation = null,
            isStudyOpen = false,
            openStudy = null,
        ),
    )
    val uiState: StateFlow<DayStudyUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<DayStudyUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DayStudyUiAction> = _uiAction

    private val completionPause = 700.milliseconds

    private var passages: List<PassageModel> = emptyList()
    private var dayRoute: DayNavRoute? = null
    private var label: String = ""
    private var jobKey: String? = null
    private var isPro: Boolean = false
    private var observeCardJob: Job? = null
    private var observeJobJob: Job? = null

    fun onEvent(event: DayStudyUiEvent) {
        when (event) {
            is DayStudyUiEvent.OnStart -> onStart(event.passages, event.dayRoute, event.label)
            DayStudyUiEvent.OnCardClick -> onCardClick()
            DayStudyUiEvent.OnStudyDismiss -> onStudyDismiss()
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
            _uiState.update { it.copy(generation = null, isStudyOpen = false, openStudy = null) }
        }
        if (generationCoordinator.pendingOpenKey.value == key) {
            generationCoordinator.consumePendingOpen(key)
            _uiState.update { it.copy(isStudyOpen = true) }
        }
        observeCard()
        observeJob()
    }

    private fun observeCard() {
        observeCardJob?.cancel()
        observeCardJob = viewModelScope.launch {
            combine(
                observeAuthenticatedUserId(),
                observeIsProUser(),
            ) { userId, pro -> userId to pro }
                .distinctUntilChanged()
                .collectLatest { (_, pro) ->
                    isPro = pro
                    refreshCard(pro)
                }
        }
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
            null -> _uiState.update { it.copy(generation = null) }

            DayStudyGenerationStatus.Generating -> _uiState.update {
                it.copy(generation = DayStudyGenerationUiModel(job.phase?.toPhaseIndex() ?: 0))
            }

            is DayStudyGenerationStatus.Done -> onJobDone(status.study)

            is DayStudyGenerationStatus.Failed -> onJobFailed(status.isLimitReached)
        }
    }

    private suspend fun onJobDone(study: DayStudyModel) {
        val key = jobKey ?: return
        if (_uiState.value.isStudyOpen) {
            if (_uiState.value.generation != null) completeGenerationPhases()
            _uiState.update { it.copy(generation = null, openStudy = study, isStudyOpen = true) }
            trackStudyOpened(isCached = false)
        } else {
            _uiState.update { it.copy(generation = null, openStudy = study) }
        }
        refreshCard(isPro)
        // The day is on screen; its own card/sheet now owns the study, so remove the job
        // (the study is cached — a later visit reopens it normally).
        generationCoordinator.acknowledge(key)
    }

    private suspend fun onJobFailed(isLimitReached: Boolean) {
        val key = jobKey ?: return
        val wasOpen = _uiState.value.isStudyOpen
        _uiState.update { it.copy(generation = null, isStudyOpen = false) }
        if (isLimitReached) lockCard()
        if (wasOpen) {
            _uiAction.emit(
                DayStudyUiAction.ShowSnackBar(
                    if (isLimitReached) Res.string.ai_study_limit_reached_message else Res.string.ai_study_error,
                ),
            )
        }
        generationCoordinator.acknowledge(key)
    }

    private suspend fun refreshCard(pro: Boolean) {
        val quota = getDayStudyQuota(passages)
        _uiState.update { state ->
            state.copy(
                card = Loadable.Loaded(
                    cardUiModelFactory.create(
                        isPro = pro,
                        quota = quota,
                    ),
                ),
            )
        }
    }

    private fun onCardClick() {
        val card = _uiState.value.card.valueOrNull() ?: return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_CARD_CLICKED,
            params = mapOf(
                AnalyticsParams.CARD_MODE to card.mode.name.lowercase(),
                AnalyticsParams.IS_PRO to card.isPro,
            ),
        )
        if (_uiState.value.openStudy != null || _uiState.value.generation != null) {
            if (_uiState.value.openStudy != null) trackStudyOpened(isCached = true)
            _uiState.update { it.copy(isStudyOpen = true) }
            return
        }
        when (card.mode) {
            DayStudyCardMode.LOCKED -> emitAction(DayStudyUiAction.NavigateToPaywall)
            DayStudyCardMode.GENERATE -> generateIfLoggedIn()
            DayStudyCardMode.VIEW -> generateOrOpen()
        }
    }

    private fun generateIfLoggedIn() {
        viewModelScope.launch {
            if (observeAuthenticatedUserId().first() == null) {
                _uiAction.emit(DayStudyUiAction.NavigateToLoginWarning)
            } else {
                startGenerationOrCachedOpen()
            }
        }
    }

    private fun generateOrOpen() {
        viewModelScope.launch { startGenerationOrCachedOpen() }
    }

    private suspend fun startGenerationOrCachedOpen() {
        val route = dayRoute ?: return
        if (hasCachedStudy(passages)) {
            openCachedStudy()
            return
        }
        if (!isConnected()) {
            trackEvent(
                name = AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED,
                params = dayParams(route) + mapOf(
                    AnalyticsParams.REASON to OFFLINE_REASON,
                    AnalyticsParams.IS_PRO to isPro,
                ),
            )
            _uiAction.emit(DayStudyUiAction.ShowSnackBar(Res.string.ai_study_offline_message))
            return
        }
        val quota = getDayStudyQuota(passages)
        if (!canStartFreeGeneration(quota)) return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_GENERATION_STARTED,
            params = dayParams(route) + mapOf(
                AnalyticsParams.IS_PRO to isPro,
                AnalyticsParams.REMAINING_FREE to quota.remainingFree,
            ),
        )
        jobKey = generationCoordinator.start(passages, route, label)
        _uiState.update {
            it.copy(
                generation = DayStudyGenerationUiModel(currentPhaseIndex = 0),
                isStudyOpen = true,
            )
        }
    }

    private suspend fun canStartFreeGeneration(quota: DayStudyQuotaModel): Boolean {
        if (isPro || quota.isUnlockedForDay) return true
        val inFlight = generationCoordinator.generatingCount(excludingKey = jobKey)
        if (inFlight < quota.remainingFree) return true
        _uiAction.emit(
            DayStudyUiAction.ShowSnackBarPlural(
                resource = Res.plurals.ai_study_wait_for_generations,
                count = inFlight,
            ),
        )
        return false
    }

    private suspend fun openCachedStudy() {
        val study = getDayStudy(passages)
            .mapNotNull { (it as? DayStudyGenerationEventModel.Completed)?.study }
            .first()
        _uiState.update { it.copy(openStudy = study, isStudyOpen = true) }
        trackStudyOpened(isCached = true)
    }

    private fun onStudyDismiss() {
        _uiState.update { it.copy(isStudyOpen = false) }
    }

    private suspend fun completeGenerationPhases() {
        _uiState.update { state ->
            state.copy(
                generation = state.generation?.copy(
                    currentPhaseIndex = DayStudyGenerationPhase.entries.size,
                ),
            )
        }
        delay(completionPause)
    }

    private fun lockCard() {
        _uiState.update { state ->
            val card = state.card.valueOrNull() ?: return@update state
            state.copy(
                card = Loadable.Loaded(
                    card.copy(
                        mode = DayStudyCardMode.LOCKED,
                        remainingFree = 0,
                    ),
                ),
            )
        }
    }

    private fun trackStudyOpened(isCached: Boolean) {
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_OPENED,
            params = mapOf(AnalyticsParams.IS_CACHED to isCached),
        )
    }

    private fun dayParams(route: DayNavRoute): Map<String, Any> = mapOf(
        AnalyticsParams.PLAN_TYPE to route.readingPlanType,
        AnalyticsParams.WEEK_NUMBER to route.weekNumber,
        AnalyticsParams.DAY_NUMBER to route.dayNumber,
    )

    private fun emitAction(action: DayStudyUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    override fun onCleared() {
        super.onCleared()
        jobKey?.let(generationCoordinator::clearActive)
    }

    private companion object {
        const val OFFLINE_REASON = "offline"
    }
}

private fun DayStudyPhaseModel.toPhaseIndex(): Int = when (this) {
    DayStudyPhaseModel.READING -> DayStudyGenerationPhase.READING.ordinal
    DayStudyPhaseModel.CHAPTERS -> DayStudyGenerationPhase.CHAPTERS.ordinal
    DayStudyPhaseModel.CONTEXT -> DayStudyGenerationPhase.CONTEXT.ordinal
    DayStudyPhaseModel.QUESTIONS -> DayStudyGenerationPhase.QUESTIONS.ordinal
}
